package com.topcoder.productsearch.crawler;

import com.topcoder.productsearch.crawler.core.CrawlRequest;
import com.topcoder.productsearch.crawler.core.Crawler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Base class with common configurations for all unit tests.
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public abstract class BaseTest {

  /**
   * Max retries
   */
  protected static final int MAX_RETRIES = 5;

  /**
   * Retry interval
   */
  protected static final Duration RETRY_INTERVAL = Duration.ofMillis(1000);

  /**
   * Mockito rule to enable @Mock annotations
   */
  @SuppressFBWarnings(value = "UrF", justification = "JUnit Mockito Rule.")
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  /**
   * Create a new {@code CrawlerRequest} object with minimum number of arguments.
   *
   * @param crawler - crawler
   * @param url - url
   * @return the {@code CrawlerRequest} object
   */
  protected CrawlRequest createCrawlRequest(Crawler crawler, String url) {
    return createCrawlRequest(crawler, url, 0, 0);
  }

  /**
   * Create a new {@code CrawlerRequest} object for the given parameters.
   *
   * @param crawler - crawler
   * @param url - url
   * @param depth - depth
   * @param priority - priority
   * @return the {@code CrawlerRequest} object.
   */
  protected CrawlRequest createCrawlRequest(Crawler crawler, String url, int depth, int priority) {
    return new CrawlRequest(crawler, url, depth, MAX_RETRIES, RETRY_INTERVAL, priority);
  }
}
