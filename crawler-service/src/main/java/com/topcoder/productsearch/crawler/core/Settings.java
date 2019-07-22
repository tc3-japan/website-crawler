package com.topcoder.productsearch.crawler.core;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Crawler configurable properties.
 * <ol>
 * <li>requestIntervalMillis: Duration</li>
 * <li>siteTimeLimitSeconds: Duration</li>
 * <li>pageDownloadTimeoutMinutes: Duration</li>
 * <li>pageDownloadMaxRetry: int</li>
 * <li>maxCrawlDepth: int</li>
 * </ol>
 */
@Setter
@ConfigurationProperties(prefix = "crawler-settings")
public class Settings {

  /**
   * Interval between each subsequent request (milliseconds).
   */
  private long requestIntervalMills = 1000;

  /**
   * Time limit for crawling an entire single site (seconds).
   */
  private long siteTimeLimitSeconds = 300;

  /**
   * Timeout for downloading a page (minutes).
   */
  private long pageDownloadTimeoutMinutes = 5;

  /**
   * Max number of times to retry a single page.
   */
  @Getter
  private int pageDownloadMaxRetry = 5;

  /**
   * Max depth that will be allowed to crawl for a site.
   */
  @Getter
  private int maxCrawlDepth = 10;

  /**
   * Size of the download thread pool, default: 4.
   */
  @Getter
  private int downloadPoolSize = 4;

  /**
   * Size of the pending download queue, default: 100.
   */
  @Getter
  private int downloadQueueSize = 100;

  /**
   * Size of the response processing thread pool size, default: 10.
   */
  @Getter
  private int responseProcessPoolSize = 10;

  /**
   * Get a {@code Duration} object the request interval in millis.
   *
   * @return - {@code Duration} of request interval millis
   */
  public Duration getRequestInterval() {
    return Duration.ofMillis(requestIntervalMills);
  }

  /**
   * Get a {@code Duration} object the site time limit in seconds.
   *
   * @return - {@code Duration} of site time limit in seconds.
   */
  public Duration getSiteTimeLimitSeconds() {
    return Duration.ofSeconds(siteTimeLimitSeconds);
  }

  /**
   * Get a {@code Duration} object the per-page download timeout in minutes.
   *
   * @return - {@code Duration} of per-page download timeout in minutes
   */
  public Duration getPageDownloadTimeout() {
    return Duration.ofMinutes(pageDownloadTimeoutMinutes);
  }

  /**
   * Get a {@code Duration} object for the page download retry interval.
   *
   * @return - {@code Duration} of download retry interval.
   */
  public Duration getPageDownloadRetryInterval() {
    return Duration.ofMillis(requestIntervalMills);
  }
}
