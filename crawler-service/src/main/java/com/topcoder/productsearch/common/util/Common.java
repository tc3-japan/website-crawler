package com.topcoder.productsearch.common.util;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.panforge.robotstxt.CustomRobotsTxtReader;
import com.panforge.robotstxt.RobotsTxt;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.models.PageSearchCriteria;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.specifications.PageSpecification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common static class
 */
public class Common {


  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(Common.class);

  /**
   * the RobotsTxt instances
   */
  private static ConcurrentHashMap<String, RobotsTxt> robotsTxtConcurrentHashMap = new ConcurrentHashMap<>();

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
  public static String removeHashSymbolFromURL(String url) {
    int lastHash = url.lastIndexOf('#');
    if (lastHash > 0) {
      return url.substring(0, lastHash);
    }
    return url;
  }

  /**
   * check the url by robots.txt
   *
   * @param site the website instance
   * @param url the url string
   * @return the result
   */
  public static Boolean hasAccess(WebSite site, String url) {
    if (robotsTxtConcurrentHashMap.get(site.getUrl()) != null) {
      return robotsTxtConcurrentHashMap.get(site.getUrl()).query(null, url);
    }

    try {
      URL siteURL = new URL(site.getUrl());
      URL robotsURL = new URL(siteURL.getProtocol() + "://" + siteURL.getHost() + "/robots.txt");
      RobotsTxt robotsTxt = new CustomRobotsTxtReader().read(robotsURL.openStream());
      robotsTxtConcurrentHashMap.put(site.getUrl(), robotsTxt);
      return robotsTxt.query(null, url);
    } catch (IOException e) {
      logger.error("read robots.txt failed, will return true for all robots check");
      e.printStackTrace();
      return true;
    }
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
      return statusCode == 404 || statusCode == 410; // the page is broken if code == 404
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
   * @param searchCriteria the searchCriteria
   * @param parallelSize   the parallel/page size
   * @param pageRepository the page repository
   * @param processHandler the process handler
   * @throws InterruptedException when thread interrupt
   */
  public static void readAndProcessPage(PageSearchCriteria searchCriteria,
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
      cPages = Common.fetch(pageRepository, searchCriteria, pageable);
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
   * @param searchCriteria the page search searchCriteria
   * @param pageable       the page request
   * @return the paged pages
   */
  public static List<CPage> fetch(PageRepository pageRepository, PageSearchCriteria searchCriteria, Pageable pageable) {
    Page<CPage> pages = pageRepository.findAll(new PageSpecification(searchCriteria), pageable);
    logger.info("fetch pages searchCriteria = " + searchCriteria.toString());
    return pages.getContent();
  }

  public static boolean endsWithHTML(String url) {

    return url.matches(".*html");
  }

  /**
   * get first n of string
   *
   * @param content the string content
   * @param length  the n
   * @return the split content
   */
  public static String firstNOfString(String content, Integer length) {
    String[] parts = content.split(" ");
    int index = 0;
    for (int i = 0; i < parts.length; i++) {
      int newIndex = index + parts[i].length() + (i > 0 ? 1 : 0);
      if (newIndex > length) {
        break;
      }
      index = newIndex;
    }
    return content.substring(0, Math.min(content.length(), index)) + (content.length() > index ? " ..." : "");
  }

  /**
   * Get the entity null properties names.
   *
   * @param source the entity
   * @return the null properties names.
   */
  public static String[] getNullPropertyNames(Object source) {
    final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
    return Stream.of(wrappedSource.getPropertyDescriptors())
        .map(FeatureDescriptor::getName)
        .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
        .toArray(String[]::new);
  }

  /**
   * get value by name
   * @param object the object
   * @param name the field name
   * @param <T> the return type
   * @return return value
   */
  public static <T> T getValueByName(Object object, String name) {
    try {
      Method method = object.getClass().getMethod("get" + StringUtils.capitalize(name));
      return (T)method.invoke(object);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * set value by name
   *
   * @param object the object
   * @param name   the field name
   * @param v      the value
   * @param <T>    the value type
   * @return return value
   */
  public static <T> void setValueByName(Object object, String name, T v) {
    try {
      Field declaredField = object.getClass().getDeclaredField(name);
      boolean accessible = declaredField.isAccessible();
      declaredField.setAccessible(true);
      declaredField.set(object, v);
      declaredField.setAccessible(accessible);
    } catch (Exception e) {
      logger.warn(String.format("inject %s with %s into object failed, %s", name, v.toString(), e.getMessage()));
    }
  }

  /**
   * create web client
   *
   * @return the web client
   */
  public static WebClient createWebClient() {
    WebClient webClient = new WebClient(new BrowserVersion.BrowserVersionBuilder(BrowserVersion.CHROME).build());
    webClient.getOptions().setJavaScriptEnabled(false);
    webClient.getOptions().setThrowExceptionOnScriptError(false);
    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    webClient.getOptions().setCssEnabled(false);
    webClient.getOptions().setRedirectEnabled(true);
    return webClient;
  }
}
