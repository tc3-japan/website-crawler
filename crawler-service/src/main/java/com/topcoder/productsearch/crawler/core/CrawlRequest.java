package com.topcoder.productsearch.crawler.core;

import com.topcoder.productsearch.common.entity.Page;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * A crawling request contains the required information for downloading and parsing a page.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(of = "url")
public final class CrawlRequest {

  /**
   * Comparator used to determine the priority of requests.
   */
  public static final Comparator<CrawlRequest> COMPARATOR = Comparator
      .comparingInt(CrawlRequest::getPriority).reversed()
      .thenComparingInt(CrawlRequest::getDepth)
      .thenComparing(CrawlRequest::getUrl);

  /**
   * The crawler that initiated the page download.
   */
  private final Crawler crawler;

  /**
   * The url to download.
   */
  private final String url;

  /**
   * Depth of the request.
   */
  private final int depth;

  /**
   * Maximum number of retries on server failure.
   */
  private final int maxRetries;

  /**
   * Time to wait before each retry.
   */
  private final Duration retryInterval;

  /**
   * Priority of the request.
   */
  private final int priority;

  /**
   * Number of attempts made so far.
   */
  private int numAttempts = 0;

  /**
   * Result of the download.
   */
  @Setter
  private ResponseEntity<String> responseEntity = null;

  /**
   * Exception during download.
   */
  @Setter
  private Exception exception;

  /**
   * Existing Page entity in the database.
   */
  @Setter
  private Page existingPage;

  /**
   * Increment number of attempts and compare the old value with maxRetries, return true if it's
   * less than max retries.
   *
   * @return true if the downloader should retry
   */
  boolean retry() {
    return numAttempts++ < maxRetries;
  }

  /**
   * Process the request with the initiating crawler and the given context.
   *
   * @param crawlContext - the crawl context
   */
  void process(CrawlContext crawlContext) {
    crawler.process(this, crawlContext);
  }

  /**
   * Get the response body.
   *
   * @return the response body
   * @throws IllegalStateException if the response entity is null.
   */
  public String getResponseBody() {
    return Optional.ofNullable(getResponseEntity())
        .map(ResponseEntity::getBody).orElseThrow(IllegalStateException::new);
  }

  /**
   * Get the response headers.
   *
   * @return the response headers
   * @throws IllegalStateException if the response entity is null.
   */
  public HttpHeaders getResponseHeaders() {
    return Optional.ofNullable(getResponseEntity())
        .map(ResponseEntity::getHeaders).orElseThrow(IllegalStateException::new);
  }

  /**
   * Get the response status code.
   *
   * @return the response status code
   * @throws IllegalStateException if the response entity is null.
   */
  public HttpStatus getResponseStatus() {
    return Optional.ofNullable(getResponseEntity())
        .map(ResponseEntity::getStatusCode)
        .orElseThrow(IllegalStateException::new);
  }
}
