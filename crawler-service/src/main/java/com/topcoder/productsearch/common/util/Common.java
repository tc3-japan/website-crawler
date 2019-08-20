package com.topcoder.productsearch.common.util;

import com.panforge.robotstxt.RobotsTxt;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.PageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Common static class
 */
public class Common {


  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(Common.class);

  /**
   * the RobotsTxt instance
   */
  private static RobotsTxt robotsTxt = null;

  /**
   * regex pattern to filter out the unnecessary link
   */
  private static final Pattern ignorePattern = Pattern
      .compile("css|js|bmp|gif|jpe?g|png" +
          "|tiff|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz", Pattern.CASE_INSENSITIVE);

  /**
   * remove hash from url
   *
   * @param url the url
   * @return processed url
   */
  public static String removeHashFromURL(String url) {
    int lastHash = url.lastIndexOf('#');
    if (lastHash > 0) {
      return url.substring(0, lastHash);
    }
    return url;
  }

  /**
   * check the url by robots.txt
   *
   * @param url the url string
   * @return the result
   */
  public synchronized static Boolean hasAccess(String url) {
    if (robotsTxt == null) {
      try {
        InputStream inputStream = Common.class.getClassLoader().getResourceAsStream("robots.txt");
        robotsTxt = RobotsTxt.read(inputStream);
      } catch (IOException e) {
        logger.error("read robots.txt failed, will return true for all robots check");
        e.printStackTrace();
        robotsTxt = null;
        return true;
      }
    }
    return robotsTxt.query(null, url);
  }

  /**
   * check is matched from website url patterns
   *
   * @param webSite the website
   * @param url     the url
   * @return the result
   */
  public static boolean isMatch(WebSite webSite, String url) {
    return url.matches(webSite.getContentUrlPatterns());
  }

  /**
   * normalize to remove cgid params from url, so that we can check two url is same url or not
   *
   * @param url the url
   * @return the updated url
   */
  public static String normalize(String url) {
    // TODO: The site-specific code
    String[] parts = url.split("&");
    return Arrays.stream(parts).filter(part -> !part.startsWith("cgid=")).collect(Collectors.joining("&"));
  }

  /**
   * check the url is unnecessary or not
   * @param url the url
   * @return  the result
   */
  public static boolean isUnnecessary(String url) {
    if (url == null) {
      return true;
    }

    String[] parts = url.split(Pattern.quote("."));
    if (parts.length <= 1) {
      return false;
    }

    String ext = parts[parts.length - 1];
    if (ignorePattern.matcher(ext).matches()) {
      return true;
    }
    return false;
  }


  /**
   * check url is broken or not (fast)
   *
   * @param url the url string
   * @return the result
   */
  public static boolean isUrlBroken(String url) {
    try {
      HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
      int statusCode = http.getResponseCode();
      return statusCode >= 400; // the page is broken if code >= 400
    } catch (MalformedURLException e) {
      // page is broken if url is invalid
      return true;
    } catch (IOException e) {
      // network error, skip this
      // Broken URL are only URLs no longer on the website.
      return false;
    }
  }

  /**
   * Process interface used in thread pool
   */
  public interface ProcessHandler {
    void process(ThreadPoolExecutor threadPoolExecutor, CPage cPage);
  }

  /**
   * read and process pages
   * 1. create a thread pool
   * 2. fetch parallelSize pages in one time
   * 3. and process this pages in multiple thread
   * 4. add page number, then fetch and process again util no pages
   *
   * @param webSiteId      the website id, can be null
   * @param parallelSize   the parallel/page size
   * @param pageRepository the page repository
   * @param processHandler the process handler
   * @throws InterruptedException when thread interrupt
   */
  public static void readAndProcessPage(Integer webSiteId,
                                        int parallelSize,
                                        PageRepository pageRepository,
                                        ProcessHandler processHandler
  ) throws InterruptedException {


    PageRequest pageable = new PageRequest(0, parallelSize);
    List<CPage> cPages;

    final int totalQueueSize = parallelSize * 3;
    int totalTask = 0;
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(parallelSize,
        parallelSize * 2, 0L,
        TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(totalQueueSize));

    do {
      cPages = Common.fetch(pageRepository, webSiteId, pageable);
      if (cPages.size() <= 0) {
        break;
      }

      while (totalQueueSize - threadPoolExecutor.getQueue().size() <= cPages.size()) {
        Thread.sleep(10); // sleep util threadPoolExecutor queue have enough size
      }
      totalTask += cPages.size();
      logger.info("add " + parallelSize + " tasks, current total number = "
          + totalTask + ", current page = " + pageable.getPageNumber());
      for (CPage cPage : cPages) {
        processHandler.process(threadPoolExecutor, cPage);
      }
      // next page
      pageable = new PageRequest(pageable.getPageNumber() + 1, pageable.getPageSize());
    } while (true);


    // wait util all task finished
    threadPoolExecutor.shutdown();
    while (!threadPoolExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
      logger.info("Awaiting completion of threads...");
    }

  }

  /**
   * fetch pages
   *
   * @param pageRepository the page repository
   * @param webSiteId      the website id
   * @param pageable       the page request
   * @return the paged pages
   */
  public static List<CPage> fetch(PageRepository pageRepository, Integer webSiteId, Pageable pageable) {
    List<CPage> pages;
    if (webSiteId != null) {
      pages = pageRepository.findAllBySiteId(webSiteId, pageable);
    } else {
      Page<CPage> dbPage = pageRepository.findAll(pageable);
      pages = dbPage.getContent();
    }
    return pages;
  }
}
