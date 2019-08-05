package com.topcoder.productsearch.common.util;

import com.topcoder.productsearch.common.entity.WebSite;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Common static class
 */
public class Common {


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
}
