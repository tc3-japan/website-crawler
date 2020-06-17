package com.topcoder.productsearch.crawler;


import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.DestinationURL;
import com.topcoder.productsearch.common.entity.SourceURL;
import com.topcoder.productsearch.common.repository.DestinationURLRepository;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.repository.SourceURLRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.common.util.DomHelper;
import com.topcoder.productsearch.common.util.SpringTool;
import com.topcoder.productsearch.crawler.service.CrawlerService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Crawler Thread class
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CrawlerThread implements Runnable {

  /**
   * the logger
   */
  private static final Logger logger = LoggerFactory.getLogger(CrawlerThread.class);

  /**
   * the task model
   */
  private CrawlerTask crawlerTask;

  /**
   * the web client
   */
  private WebClient webClient;

  /**
   * the params for task wait time
   */
  private Integer taskInterval;

  /**
   * task timeout
   */
  private Integer timeout;

  /**
   * task depth
   */
  private Integer maxDepth;

  /**
   * task retry times
   */
  private int retryTimes;

  /**
   * crawler service
   */
  private CrawlerService crawlerService;

  /**
   * page database repository
   */
  private PageRepository pageRepository;

  /**
   * destinationURL database repository
   */
  private DestinationURLRepository destinationURLRepository;

  /**
   * the source url database repository
   */
  private SourceURLRepository sourceURLRepository;

  /**
   * parsed urls that maybe need create new task to download
   */
  private Set<String> expandUrl;

  /**
   * the dom helper
   */
  private DomHelper domHelper;


  /**
   * init thread
   */
  public void init() {
    webClient = Common.createWebClient();
    // set the webclient timeout, unit is milliseconds
    webClient.getOptions().setTimeout(timeout);
    // set enable/disable Javascript (default: false)
    if (crawlerTask.getSite().getSupportsJs() != null) {
      webClient.getOptions().setJavaScriptEnabled(crawlerTask.getSite().getSupportsJs());
    }
    if (SpringTool.getApplicationContext() != null) {
      pageRepository = SpringTool.getApplicationContext().getBean(PageRepository.class);
      destinationURLRepository = SpringTool.getApplicationContext().getBean(DestinationURLRepository.class);
      sourceURLRepository = SpringTool.getApplicationContext().getBean(SourceURLRepository.class);
    }
    domHelper = new DomHelper();
    expandUrl = new HashSet<>();
  }


  /**
   * thread run
   */
  @Override
  public void run() {
    crawlerTask.setStartTime(System.currentTimeMillis());
    try {
      download(new WebRequest(new URL(crawlerTask.getUrl())));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  /**
   * wait some time
   */
  private void waitInterval() {
    // sleep interval between each subsequent request (milliseconds)
    try {
      Thread.sleep(taskInterval);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * process page
   *
   * @param page the html page
   */
  protected void handlingPage(HtmlPage page) {

    Integer pageId = null;
    if (Common.isMatch(crawlerTask.getSite(), crawlerTask.getUrl())) {
      String pageUrl = Common.normalize(crawlerTask.getUrl());
      CPage dbPage = pageRepository.findByUrl(pageUrl);

      if (dbPage == null) {
        dbPage = new CPage();
        dbPage.setCreatedAt(Date.from(Instant.now()));
      }
      dbPage.setLastModifiedAt(Date.from(Instant.now()));

      dbPage.setUrl(pageUrl);
      dbPage.setSiteId(crawlerTask.getSite().getId());
      dbPage.setType("product");
      dbPage.setTitle(page.getTitleText());
      dbPage.setBody(page.getBody().asXml());
      dbPage.setContent(domHelper.getContentsByCssSelectors(page,
              crawlerTask.getSite().getContentSelector()));
      dbPage.setCategory(domHelper.getCategoryByPattern(page.getBody().asXml(),
              crawlerTask.getSite().getCategoryExtractionPattern()));
      dbPage.setEtag(page.getWebResponse().getResponseHeaderValue("ETag"));
      dbPage.setLastModified(page.getWebResponse().getResponseHeaderValue("Last-Modified"));
      pageRepository.save(dbPage);

      pageId = dbPage.getId();
      logger.info("saved page = " + pageId + ", url = " + pageUrl);
    }

    // find all links
    Integer finalPageId = pageId;
    List<String> urls = domHelper.findAllUrls(page);
    urls.forEach(urlLink -> {
      String homeUrl = crawlerTask.getSite().getUrl();

      if (urlLink.startsWith("//")) {
        // add the schema (http: or https:)
        String[] part1s = homeUrl.split("//");
        urlLink = part1s[0] + urlLink;
      }

      if (urlLink.startsWith("http") && !urlLink.contains(homeUrl)) {
        // Filter out external URLs
        return;
      }

      if (!urlLink.startsWith("/") && !urlLink.contains(homeUrl)) {
        return;
      }

      if (urlLink.startsWith("/")) { // relative url
        String[] part1s = homeUrl.split("//");
        String[] part2s = part1s[1].split("/");
        urlLink = part1s[0] + "//" + part2s[0] + urlLink;
      }
      urlLink = Common.removeHashSymbolFromURL(urlLink);

      if (Common.isUnnecessary(urlLink)) {
        return;
      }

      if (urlLink.endsWith("/orders") || urlLink.endsWith(".pdf")) {
        return;
      }


      // Create records in the source_urls table with following data
      if (crawlerTask.getSourceUrl() != null && finalPageId != null) {
        SourceURL sourceURL = sourceURLRepository.findByUrlAndPageId(crawlerTask.getSourceUrl(), finalPageId);
        if (sourceURL == null) {
          sourceURL = new SourceURL();
          sourceURL.setUrl(crawlerTask.getSourceUrl());
          sourceURL.setPageId(finalPageId);
          sourceURL.setCreatedAt(Date.from(Instant.now()));
        } else {
          sourceURL.setLastModifiedAt(Date.from(Instant.now()));
        }
        sourceURLRepository.save(sourceURL);
      }

      if (finalPageId != null) {
        DestinationURL destinationURL = destinationURLRepository.findByUrlAndPageId(urlLink, finalPageId);
        // page already have been processed from a previous thread during the execution and should be skipped.
        // only process not exist destinationURL
        if (destinationURL == null) {
          destinationURL = new DestinationURL();
          destinationURL.setCreatedAt(Date.from(Instant.now()));
          destinationURL.setUrl(urlLink);
          destinationURL.setPageId(finalPageId);
          destinationURLRepository.save(destinationURL);
          enqueue(urlLink);
        }
      } else {
        enqueue(urlLink);
      }
    });
  }

  /**
   * put url into hashSet
   *
   * @param url the url
   */
  private void enqueue(String url) {
    if (crawlerService != null
        && crawlerService.getThreadPoolExecutor().hasReachedTimeLimit(crawlerTask.getSite().getCrawlTimeLimit())) {
      logger.info("skip " + url + " , because of reached time limit");
      return;
    }
    if (crawlerTask.getDepth() >= maxDepth) {
      logger.info("skip " + url + " , because of reached max depth");
      return;
    }
    if (Boolean.TRUE.equals(crawlerTask.getSite().getSupportsRobotsTxt())
        && !Common.hasAccess(crawlerTask.getSite(), url)) {
      logger.info("skip " + url + " , because of robots.txt disallow this");
      return;
    }

    expandUrl.add(url);
  }

  /**
   * retry download page
   *
   * @param request the webRequest
   */
  private void retry(WebRequest request) {
    if (retryTimes > 0) {
      logger.info("download failed for url, wait some time and retry, count = " + retryTimes);
      waitInterval();
      retryTimes -= 1;
      download(request);
    }
  }

  /**
   * download page
   *
   * @param request the webRequest
   */
  public void download(WebRequest request) {
    String url = getCrawlerTask().getUrl();
    try {
      request.setUrl(new URL(url));
      CPage dbPage = pageRepository.findByUrl(url);

      if (dbPage != null) {
        request.setAdditionalHeader("If-Modified-Since", dbPage.getLastModified());
        request.setAdditionalHeader("If-None-Match", dbPage.getEtag());
      }

      if (crawlerTask.getSite().getSupportsJs()) {
        // Enabling JS engine if the page is a product page.
        boolean enableJS = Common.isMatch(crawlerTask.getSite(), crawlerTask.getUrl());
        webClient.getOptions().setJavaScriptEnabled(enableJS);
      }

      logger.info("start download page = " + url + ", js-enabled: " + webClient.getOptions().isJavaScriptEnabled());

      HtmlPage page = webClient.getPage(request);
      int code = page.getWebResponse().getStatusCode();

      if (System.currentTimeMillis() - crawlerTask.getStartTime() > timeout) {
        throw new Exception("timeout exception for url " + crawlerTask.getUrl());
      }

      if (code <= 204) { // think download successful
        handlingPage(page);
      } else if (code == 304) {
        // When getting 304 response (Not Modified), skip further processing for this page, and output a log
        logger.info("got 304 for url = " + url + ", so skip this url");
      } else if (code < 400 && page.getWebResponse().getResponseHeaderValue("Location") != null) {
        // When getting some 30x response and "Location" header,  enqueue a new request for the URL in Location header
        expandUrl.add(page.getWebResponse().getResponseHeaderValue("Location"));
      } else if (code >= 400 && code < 500) {
        logger.error("unexpected status code = " + code);
        logger.error(page.getTitleText());
      } else if (code >= 500) {
        logger.warn("Received Internal Server Error: "+code+" from target Website Url: "+url);
        logger.info("Retrying url: "+url);
        this.retry(request);
      }
    } catch (Exception e) {
      logger.error(" >>>> download failed, url=" + url + ", error=" + e.getMessage(), e);
      this.retry(request);
    }
  }
}