package com.topcoder.productsearch.crawler.core;

import java.time.Duration;
import java.util.Collection;

/**
 * The crawler interface for interacting with the crawling engine. Implementations will handle
 * extraction of url and/or items from the downloaded page.
 */
public interface Crawler {

  /**
   * Returns a collection of urls to start crawling. The collection must not be empty.
   *
   * @return - a collection of urls to start crawling.
   */
  Collection<String> getStartUrls();

  /**
   * Process the response body for a crawl request.
   *
   * @param crawlRequest - the downloaded page.
   * @param crawlContext - the context of the crawler
   */
  void process(CrawlRequest crawlRequest, CrawlContext crawlContext);

  /**
   * Mark the start time of the crawling, and setting the time limit.
   *
   * @param timeLimit - the time limit for this crawl.
   */
  void startTimer(Duration timeLimit);

  /**
   * Mark the stop time of the crawling.
   *
   * @return the time elapsed.
   */
  Duration stopTimer();
}
