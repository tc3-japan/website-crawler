package com.topcoder.productsearch.crawler.website;

import com.google.common.base.Stopwatch;
import com.topcoder.productsearch.common.entity.Page;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.crawler.core.CrawlContext;
import com.topcoder.productsearch.crawler.core.CrawlRequest;
import com.topcoder.productsearch.crawler.core.Crawler;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * Crawler for a given website. Using the website's url as the start url.
 */
@Slf4j
public abstract class WebSiteCrawler implements Crawler {

  /**
   * Page type.
   */
  private static final String PAGE_TYPE = "Product";

  /**
   * The website object.
   */
  @Getter(AccessLevel.PROTECTED)
  private final WebSite webSite;

  /**
   * The pattern that matches a content page url.
   */
  private final Pattern contentUrlPattern;

  /**
   * The stopwatch object for timing the crawl.
   */
  private final Stopwatch stopwatch = Stopwatch.createUnstarted();

  /**
   * The total time limit for crawling the website.
   */
  private Duration timeLimit;

  /**
   * Constructor that takes a website object.
   *
   * @param webSite - the website object.
   */
  protected WebSiteCrawler(WebSite webSite) {
    this.webSite = webSite;
    this.contentUrlPattern = Pattern.compile(webSite.getContentUrlPatterns());
  }

  @Override
  public void startTimer(Duration timeLimit) {
    this.timeLimit = timeLimit;
    stopwatch.start();
  }

  @Override
  public Duration stopTimer() {
    stopwatch.stop();
    return stopwatch.elapsed();
  }

  @Override
  public final Collection<String> getStartUrls() {
    return Collections.singletonList(webSite.getUrl());
  }

  @Override
  public void process(CrawlRequest crawlRequest, CrawlContext crawlContext) {
    if (crawlRequest.getResponseStatus().is3xxRedirection()) {
      handleRedirectResponse(crawlRequest, crawlContext);
    } else if (crawlRequest.getResponseStatus().is2xxSuccessful()) {
      handleSuccessResponse(crawlRequest, crawlContext);
    }
  }

  /**
   * Checks if we ran out of time.
   *
   * @return true if we exceeded the crawl time limit, false otherwise.
   */
  protected boolean isTimeLimitExceeded() {
    return !stopwatch.isRunning() || stopwatch.elapsed().compareTo(timeLimit) > 0;
  }

  /**
   * Abstract method for sub-class to implement specific crawling logic.
   *
   * @param document - the html document to extract information.
   * @return - a {@code Set} of urls.
   */
  protected abstract Set<String> crawlInternal(Document document);

  /**
   * Checks whether a given url is external.
   *
   * @param uri - the uri to check
   * @return true if the url is external and should be skipped.
   */
  protected abstract boolean isExternal(String uri);


  /**
   * Checks whether the page is a content page by matching it against the content_url_patterns.
   *
   * @param crawlRequest - the page request with server response.
   * @return true if the page is content and should be saved to database.
   */
  private boolean isContentPage(CrawlRequest crawlRequest) {
    return contentUrlPattern.matcher(crawlRequest.getUrl()).matches();
  }

  /**
   * Schedule a collection of urls for download if we haven't reached the crawl time limit.
   *
   * @param context - context
   * @param urls - urls to download
   * @param depth - depth of the urls.
   */
  private void scheduleForDownload(CrawlContext context, Set<String> urls, int depth) {
    if (!isTimeLimitExceeded()) {
      context.scheduleForDownload(this, urls, depth);
    } else {
      logger.debug("Time limit exceeded not scheduling new downloads.");
    }
  }

  /**
   * Handle the 2xx success response. Parse the body, call subclass to extract links, schedule for
   * download. Then persist the response if it's content page.
   *
   * @param crawlRequest - the crawl request containing the response.
   * @param crawlContext - the crawl context.
   */
  private void handleSuccessResponse(CrawlRequest crawlRequest, CrawlContext crawlContext) {
    Document document = Jsoup
        .parse(crawlRequest.getResponseBody(), crawlRequest.getUrl());

    Set<String> urls = crawlInternal(document);

    if (urls != null) {
      scheduleForDownload(crawlContext, urls, crawlRequest.getDepth() + 1);
    } else {
      urls = Collections.emptySet();
    }

    if (isContentPage(crawlRequest)) {
      logger.info("Found content page {}", crawlRequest.getUrl());
      Page page = createPage(crawlRequest, document);
      crawlContext.save(page, urls);
    }
  }

  /**
   * Create {@code Page} entity from crawl request and the HTML document, setting the id and
   * createdAt field if existing Page found in database.
   *
   * @param crawlRequest - crawl request
   * @param document - html document
   * @return - a new {@code Page} object.
   */
  private Page createPage(CrawlRequest crawlRequest, Document document) {
    Page page = new Page();

    Page oldPage = crawlRequest.getExistingPage();
    if (oldPage != null) {
      logger.debug("Updating existing page id: {}, created at: {}", oldPage,
          oldPage.getCreatedAt());
      page.setId(oldPage.getId());
      page.setCreatedAt(oldPage.getCreatedAt());
    }

    page.setWebSite(getWebSite());
    page.setUrl(crawlRequest.getUrl());
    page.setBody(crawlRequest.getResponseBody());
    page.setType(PAGE_TYPE);
    page.setTitle(document.title());

    HttpHeaders httpHeaders = crawlRequest.getResponseHeaders();
    if (httpHeaders.getLastModified() >= 0) {
      page.setLastModified(new Date(httpHeaders.getLastModified()));
    }
    if (httpHeaders.getETag() != null) {
      page.setEtag(httpHeaders.getETag());
    }
    return page;
  }

  /**
   * Handles 3xx redirect response. For 304 response, output a log and return. Otherwise try to
   * schedule the new location for download.
   *
   * @param crawlRequest - the request containing the response.
   * @param crawlContext - the crawl context.
   */
  private void handleRedirectResponse(CrawlRequest crawlRequest, CrawlContext crawlContext) {
    if (crawlRequest.getResponseStatus().value() == HttpStatus.NOT_MODIFIED.value()) {
      logger.info("Page not modified {}.", crawlRequest.getUrl());
      return;
    }
    String redirection = Optional.ofNullable(crawlRequest.getResponseHeaders().getLocation())
        .map(URI::toString).orElse(null);
    if (redirection != null && !isExternal(redirection)) {
      scheduleForDownload(crawlContext, Collections.singleton(redirection),
          crawlRequest.getDepth() + 1);
    } else {
      logger.debug("Redirect: skpped null/external page {}", redirection);
    }
  }

}
