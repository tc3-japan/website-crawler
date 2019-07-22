package com.topcoder.productsearch.crawler.core;

import com.topcoder.productsearch.common.entity.Page;
import java.util.Collection;

/**
 * The context object for the crawler to save crawled items (to save to database).
 */
public interface CrawlContext {

  /**
   * Save page and a collection destinationUrls to database.
   *
   * @param page - the page object to save
   * @param destinationUrl - the destinationUrls to save.
   */
  void save(Page page, Collection<String> destinationUrl);

  /**
   * Schedule a collection of urls for downloading.
   *
   * @param crawler - the requesting crawler
   * @param urls - the urls to download
   * @param depth - the depth of the urlS.
   */
  void scheduleForDownload(Crawler crawler, Collection<String> urls, int depth);
}
