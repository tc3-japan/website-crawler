package com.topcoder.productsearch.crawler.website;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.topcoder.productsearch.common.entity.Page;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.crawler.BaseTest;
import com.topcoder.productsearch.crawler.core.CrawlContext;
import com.topcoder.productsearch.crawler.core.CrawlRequest;
import com.topcoder.productsearch.crawler.core.Crawler;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@code UniqloWebSiteCrawler}
 */
public class UniqloWebSiteCrawlerTest extends BaseTest {

  /**
   * Time limit for the website crawling.
   */
  private static final Duration TIME_LIMIT = Duration.ofMillis(100);

  /**
   * Mock {@code CrawlContext}
   */
  @Mock
  private CrawlContext crawlContext;

  /**
   * Captor of page arguments
   */
  @Captor
  private ArgumentCaptor<Page> pageCaptor;

  /**
   * Captor of urls argument
   */
  @Captor
  private ArgumentCaptor<Set<String>> urlsCaptor;

  /**
   * A website object to pass to the constructor.
   */
  private WebSite webSite = new WebSite();

  /**
   * Object under test.
   */
  private UniqloWebSiteCrawler uniqloWebSiteCrawler;

  @Before
  public void setUp() {
    webSite.setName("UNIQLO");
    webSite.setDescription("Shop UNIQLO.com for the latest essentials for women, men,"
        + " kids &amp; babies. Clothing with innovation and real value, engineered to"
        + " enhance your life every day, all year round. UNIQLO US.");
    webSite.setUrl("http://www.uniqlo.com/us/en/home");
    webSite.setContentUrlPatterns("https?://www.uniqlo.com/us/en/[^/]*[0-9]+.html.*$");
    uniqloWebSiteCrawler = new UniqloWebSiteCrawler(webSite);
  }

  /**
   * Test the process
   */
  @Test
  public void testProcess_Normal() {
    Date lastModified = new Date();
    String responseBody = "<html>"
        + "<head>"
        + "<title>Title</title>"
        + "</head>"
        + "<body>"
        + "<a href=\"/us/en/t-shirt-x\">T-Shirt</a>"
        + "<a href=\"/us/en/t-shirt-x\">T-Shirt</a>"
        + "</body>"
        + "</html>";
    CrawlRequest crawlRequest = new CrawlRequest(uniqloWebSiteCrawler,
        "https://www.uniqlo.com/us/en/women-high-rise-skinny-flare-ankle-jeans-420408.html", 1, 1,
        Duration.ofMillis(0), 0);
    HttpHeaders headers = new HttpHeaders();
    headers.setETag("W/test-e-tag\"");
    headers.setLastModified(lastModified.getTime());
    crawlRequest.setResponseEntity(new ResponseEntity<>(responseBody, headers, HttpStatus.OK));

    doNothing()
        .when(crawlContext).save(any(Page.class), anyListOf(String.class));
    doNothing()
        .when(crawlContext).scheduleForDownload(any(Crawler.class), anyListOf(String.class), eq(2));

    uniqloWebSiteCrawler.startTimer(TIME_LIMIT);
    uniqloWebSiteCrawler.process(crawlRequest, crawlContext);
    uniqloWebSiteCrawler.stopTimer();

    verify(crawlContext, times(1)).save(pageCaptor.capture(), urlsCaptor.capture());
    verify(crawlContext, times(1))
        .scheduleForDownload(same(uniqloWebSiteCrawler), urlsCaptor.capture(), eq(2));

    Assert.assertThat(pageCaptor.getValue().getTitle(), Matchers.is("Title"));
    Assert.assertThat(pageCaptor.getValue().getUrl(), Matchers.is(crawlRequest.getUrl()));
    Assert.assertThat(pageCaptor.getValue().getType(), Matchers.is("Product"));
    Assert.assertThat(pageCaptor.getValue().getEtag(), Matchers.is(headers.getETag()));
    Assert.assertThat(pageCaptor.getValue().getLastModified(),
        Matchers.is(new Date(headers.getLastModified())));
    Assert.assertThat(pageCaptor.getValue().getWebSite(), Matchers.sameInstance(webSite));

    Assert.assertThat(urlsCaptor.getAllValues(), Matchers.hasSize(2));
    Assert.assertThat(urlsCaptor.getAllValues().get(0), Matchers.hasSize(1));
    Assert.assertThat(urlsCaptor.getAllValues().get(0).iterator().next(),
        Matchers.is("https://www.uniqlo.com/us/en/t-shirt-x"));
    Assert.assertThat(urlsCaptor.getAllValues().get(1), Matchers.hasSize(1));
    Assert.assertThat(urlsCaptor.getAllValues().get(1).iterator().next(),
        Matchers.is("https://www.uniqlo.com/us/en/t-shirt-x"));
  }

  /**
   * Crawler should not schedule urls for download if its time limit has exceeded.
   */
  @Test
  public void testProcess_No_ScheduleForDownload_If_TimeLimit_Exceeded()
      throws InterruptedException {
    Date lastModified = new Date();
    String responseBody = "<html>"
        + "<head>"
        + "<title>Title</title>"
        + "</head>"
        + "<body>"
        + "<a href=\"/us/en/t-shirt-x\">T-Shirt</a>"
        + "<a href=\"/us/en/t-shirt-x\">T-Shirt</a>"
        + "</body>"
        + "</html>";
    CrawlRequest crawlRequest = new CrawlRequest(uniqloWebSiteCrawler,
        "https://www.uniqlo.com/us/en/women-high-rise-skinny-flare-ankle-jeans-420408.html", 1, 1,
        Duration.ofMillis(0), 0);
    HttpHeaders headers = new HttpHeaders();
    headers.setETag("W/test-e-tag\"");
    headers.setLastModified(lastModified.getTime());
    crawlRequest.setResponseEntity(new ResponseEntity<>(responseBody, headers, HttpStatus.OK));

    doNothing()
        .when(crawlContext).save(any(Page.class), anyListOf(String.class));

    uniqloWebSiteCrawler.startTimer(Duration.ofMillis(1));
    Thread.sleep(5);

    uniqloWebSiteCrawler.process(crawlRequest, crawlContext);
    uniqloWebSiteCrawler.stopTimer();

    verify(crawlContext, times(1)).save(pageCaptor.capture(), urlsCaptor.capture());
    verify(crawlContext, never())
        .scheduleForDownload(eq(uniqloWebSiteCrawler), urlsCaptor.capture(), eq(2));

    Assert.assertThat(pageCaptor.getValue().getTitle(), Matchers.is("Title"));
    Assert.assertThat(pageCaptor.getValue().getUrl(), Matchers.is(crawlRequest.getUrl()));
    Assert.assertThat(pageCaptor.getValue().getType(), Matchers.is("Product"));
    Assert.assertThat(pageCaptor.getValue().getEtag(), Matchers.is(headers.getETag()));
    Assert.assertThat(pageCaptor.getValue().getLastModified(),
        Matchers.is(new Date(headers.getLastModified())));
    Assert.assertThat(pageCaptor.getValue().getWebSite(), Matchers.sameInstance(webSite));

    Assert.assertThat(urlsCaptor.getAllValues(), Matchers.hasSize(1));
  }

  /**
   * Crawler should schedule the redirect location if it's not external and within time limit.
   */
  @Test
  public void testProcess_Redirect() {
    CrawlRequest crawlRequest = new CrawlRequest(uniqloWebSiteCrawler,
        "http://www.uniqlo.com/us/en/url1", 1, 1, Duration.ofMillis(0), 0);
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create("http://www.uniqlo.com/us/en/url-redirected"));
    crawlRequest.setResponseEntity(new ResponseEntity<>("", headers, HttpStatus.FOUND));

    doNothing()
        .when(crawlContext)
        .scheduleForDownload(eq(uniqloWebSiteCrawler), anyListOf(String.class), eq(2));

    uniqloWebSiteCrawler.startTimer(TIME_LIMIT);
    uniqloWebSiteCrawler.process(crawlRequest, crawlContext);

    verify(crawlContext, times(1))
        .scheduleForDownload(eq(uniqloWebSiteCrawler), urlsCaptor.capture(), eq(2));

    Assert.assertThat(urlsCaptor.getValue(), Matchers.hasSize(1));
    Assert.assertThat(urlsCaptor.getValue().iterator().next(),
        Matchers.is("http://www.uniqlo.com/us/en/url-redirected"));
  }

  /**
   * Test the builtin stop watch and the isTimeLimitExceeded method.
   */
  @Test
  public void testStopWatch() throws InterruptedException {
    long sleepTimeMillis = TIME_LIMIT.toMillis();

    uniqloWebSiteCrawler.startTimer(TIME_LIMIT);

    // Time limit not exceeded at this time.
    Assert.assertThat(uniqloWebSiteCrawler.isTimeLimitExceeded(), Matchers.is(false));

    // Sleep for a while and stop the crawler.
    Thread.sleep(sleepTimeMillis);
    // And time limit exceeded.
    Assert.assertThat(uniqloWebSiteCrawler.isTimeLimitExceeded(), Matchers.is(true));
    Duration duration = uniqloWebSiteCrawler.stopTimer();

    // Assert the time elapsed recorded by the crawler >= sleep time.
    Assert.assertThat(duration.toMillis(), Matchers.greaterThanOrEqualTo(sleepTimeMillis));
    // And time limit is still exceeded.
    Assert.assertThat(uniqloWebSiteCrawler.isTimeLimitExceeded(), Matchers.is(true));
  }

  /**
   * Test the isExternal method
   */
  @Test
  public void testIsExternal() {
    Assert.assertTrue(uniqloWebSiteCrawler.isExternal("https://www.example.com"));
    Assert.assertTrue(uniqloWebSiteCrawler.isExternal("https://www.uniqlo.com/au/home/"));
    Assert.assertTrue(uniqloWebSiteCrawler.isExternal("https://www.uniqlo.com/us/cn/baby"));

    Assert.assertFalse(uniqloWebSiteCrawler.isExternal("http://www.uniqlo.com/us/en/baby"));
    Assert.assertFalse(uniqloWebSiteCrawler.isExternal(
        "https://www.uniqlo.com/us/en/women-u-crew-neck-short-sleeve-t-shirt-421301.html"));
  }

  /**
   * Test getStartUrls method
   */
  @Test
  public void testGetStartUrls() {
    Collection<String> startUrls = uniqloWebSiteCrawler.getStartUrls();

    Assert.assertThat(startUrls, Matchers.hasSize(1));
    Assert.assertThat(startUrls.iterator().next(), Matchers.is(webSite.getUrl()));
  }
}