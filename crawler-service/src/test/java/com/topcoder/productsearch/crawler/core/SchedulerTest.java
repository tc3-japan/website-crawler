package com.topcoder.productsearch.crawler.core;

import com.topcoder.productsearch.crawler.BaseTest;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Unit tests for {@code Scheduler}
 */
public class SchedulerTest extends BaseTest {

  /**
   * Mock crawler
   */
  @Mock
  private Crawler crawler;

  /**
   * The object under test.
   */
  private Scheduler scheduler;

  @Before
  public void setUp() {
    scheduler = new Scheduler(new Deduplicator());
  }

  /**
   * Scheduler should put requests with higher priority first
   */
  @Test
  public void testPriority() {
    String url1 = "http://www.example.com/url1";
    String url2 = "http://www.example.com/url2";

    scheduler.schedule(createCrawlRequest(crawler, url1, 0, 1));
    scheduler.schedule(createCrawlRequest(crawler, url2, 0, 2));

    CrawlRequest firstRequest = scheduler.getNextRequest();
    CrawlRequest secondRequest = scheduler.getNextRequest();

    // Verify that url2 is scheduled first because its priority is greater than url1.
    Assert.assertThat(firstRequest.getUrl(), Matchers.is(url2));
    Assert.assertThat(secondRequest.getUrl(), Matchers.is(url1));


  }

  /**
   * When requests have same priority, the one with lower depth win.
   */
  @Test
  public void testPriorityTieBreakByDepth() {
    String url1 = "http://www.example.com/url1";
    String url2 = "http://www.example.com/url2";

    // When requests have the same priority, urls with lower depths takes precedence.

    scheduler.schedule(createCrawlRequest(crawler, url1, 2, 0));
    scheduler.schedule(createCrawlRequest(crawler, url2, 1, 0));

    CrawlRequest firstRequest = scheduler.getNextRequest();
    CrawlRequest secondRequest = scheduler.getNextRequest();

    Assert.assertThat(firstRequest.getUrl(), Matchers.is(url2));
    Assert.assertThat(secondRequest.getUrl(), Matchers.is(url1));
  }

  /**
   * When requests have same priority and depth, compare their lexicographic order.
   */
  @Test
  public void testPriorityTieBreakByUrl() {
    String url1 = "http://www.example.com/urlb";
    String url2 = "http://www.example.com/urla";

    // When requests have the same priority, urls with lower depths takes precedence.

    scheduler.schedule(createCrawlRequest(crawler, url1, 0, 0));
    scheduler.schedule(createCrawlRequest(crawler, url2, 0, 0));

    CrawlRequest firstRequest = scheduler.getNextRequest();
    CrawlRequest secondRequest = scheduler.getNextRequest();

    Assert.assertThat(firstRequest.getUrl(), Matchers.is(url2));
    Assert.assertThat(secondRequest.getUrl(), Matchers.is(url1));
  }

  /**
   * Schedule should skip duplicate url and return false.
   */
  @Test
  public void testSkippDuplicateUrls() {
    String url = "http://www.example.com/url1";
    Assert.assertTrue(scheduler.schedule(createCrawlRequest(crawler, url, 0, 0)));
    Assert.assertFalse(scheduler.schedule(createCrawlRequest(crawler, url, 1, 2)));

    // Verify that we only scheduled one request.
    Assert.assertTrue(scheduler.hasMoreRequests());
    Assert.assertNotNull(scheduler.getNextRequest());
    Assert.assertFalse(scheduler.hasMoreRequests());
  }
}