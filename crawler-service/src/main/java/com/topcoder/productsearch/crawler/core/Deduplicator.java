package com.topcoder.productsearch.crawler.core;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides deduplication of pages based on their urls.
 */
public class Deduplicator {

  /**
   * Set of urls that this filter has seen before.
   */
  private final Set<String> urls = new HashSet<>();

  /**
   * Check if a url has been seen. Adding the url to the set.
   *
   * @param url - url to check
   * @return true if we have seen this url before.
   */
  public boolean hasSeen(String url) {
    return !urls.add(url);
  }
}
