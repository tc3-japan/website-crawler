package com.topcoder.productsearch.crawler.core;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Stopwatch;
import com.topcoder.productsearch.crawler.BaseTest;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for {@code Downloader}.
 */
@SuppressWarnings("unchecked")
public class DownloaderTest extends BaseTest {

  /**
   * Queue size for downloader.
   */
  private static final int QUEUE_SIZE = 50;

  /**
   * URL
   */
  private static final String URL = "http://www.example.com";

  /**
   * Request interval in millis
   */
  private static final int REQUEST_INTERVAL_MILLS = 100;

  /**
   * Mock rest template.
   */
  @Mock
  private RestTemplate restTemplate;

  /**
   * Mock crawler.
   */
  @Mock
  private Crawler crawler;

  /**
   * The object under test.
   */
  private Downloader downloader;

  @Before
  public void setUp() {
    /*
     * The task executor.
     */
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setDaemon(true);
    taskExecutor.setMaxPoolSize(1);
    taskExecutor.initialize();

    downloader = new Downloader(taskExecutor, restTemplate,
        Duration.ofMillis(REQUEST_INTERVAL_MILLS), QUEUE_SIZE);
    downloader.start();
  }

  @After
  public void tearDown() {
    downloader.shutdown();
  }

  @Test
  public void testDownload_Successful() throws InterruptedException {
    RequestEntity<String> requestEntity = new RequestEntity<>(HttpMethod.GET, URI.create(URL));
    ResponseEntity<String> responseEntity = new ResponseEntity<>("<html>", HttpStatus.OK);

    when(restTemplate.exchange(requestEntity, String.class))
        .thenReturn(responseEntity);

    CountDownLatch countDownLatch = new CountDownLatch(1);

    CrawlRequest crawlRequest = createCrawlRequest(crawler, URL);

    downloader
        .submit(crawlRequest, cr -> countDownLatch.countDown(), cr -> countDownLatch.countDown());

    countDownLatch.await();

    Assert.assertThat(crawlRequest.getResponseStatus(), Matchers.is(HttpStatus.OK));
    Assert.assertThat(crawlRequest.getResponseBody(), Matchers.is("<html>"));
    Assert.assertNull(crawlRequest.getException());

    verify(restTemplate, times(1)).exchange(requestEntity, String.class);
  }

  @Test
  public void testDownload_ServerError_Retry() throws InterruptedException {
    String url = "http://www.example.com";
    RequestEntity<String> requestEntity = new RequestEntity<>(HttpMethod.GET, URI.create(url));
    ResponseEntity<String> internalServerError = new ResponseEntity<>("error",
        HttpStatus.INTERNAL_SERVER_ERROR);
    ResponseEntity<String> success = new ResponseEntity<>("<html>", HttpStatus.OK);

    when(restTemplate.exchange(requestEntity, String.class))
        .thenReturn(internalServerError, internalServerError, success);

    CountDownLatch countDownLatch = new CountDownLatch(1);

    CrawlRequest crawlRequest = createCrawlRequest(crawler, url, 1, 1);

    downloader
        .submit(crawlRequest, cr -> countDownLatch.countDown(), cr -> countDownLatch.countDown());

    countDownLatch.await();

    Assert.assertThat(crawlRequest.getResponseStatus(), Matchers.is(HttpStatus.OK));
    Assert.assertThat(crawlRequest.getResponseBody(), Matchers.is("<html>"));
    Assert.assertNull(crawlRequest.getException());

    verify(restTemplate, times(3)).exchange(requestEntity, String.class);
  }

  @Test
  public void testDownload_ServerError_Retry_Fail() throws InterruptedException {
    RequestEntity<String> requestEntity = new RequestEntity<>(HttpMethod.GET, URI.create(URL));
    ResponseEntity<String> internalServerError = new ResponseEntity<>("error",
        HttpStatus.INTERNAL_SERVER_ERROR);

    when(restTemplate.exchange(requestEntity, String.class))
        .thenReturn(internalServerError, internalServerError, internalServerError,
            internalServerError, internalServerError, internalServerError);

    CountDownLatch countDownLatch = new CountDownLatch(1);

    CrawlRequest crawlRequest = createCrawlRequest(crawler, URL, 1, 1);

    downloader
        .submit(crawlRequest,
            cr -> countDownLatch.countDown(),
            cr -> countDownLatch.countDown());

    countDownLatch.await();

    Assert.assertThat(crawlRequest.getResponseStatus(),
        Matchers.is(HttpStatus.INTERNAL_SERVER_ERROR));
    Assert.assertThat(crawlRequest.getResponseBody(), Matchers.is("error"));
    Assert.assertNull(crawlRequest.getException());

    verify(restTemplate, times(MAX_RETRIES + 1)).exchange(requestEntity, String.class);
  }

  @Test
  public void testDownload_Timeout_Retry() throws InterruptedException {
    RequestEntity<String> requestEntity = new RequestEntity<>(HttpMethod.GET, URI.create(URL));

    when(restTemplate.exchange(requestEntity, String.class))
        .thenThrow(new IllegalStateException("Timeout", new SocketTimeoutException()));

    CountDownLatch countDownLatch = new CountDownLatch(1);

    CrawlRequest crawlRequest = createCrawlRequest(crawler, URL, 1, 1);

    downloader
        .submit(crawlRequest,
            cr -> countDownLatch.countDown(),
            cr -> countDownLatch.countDown());

    countDownLatch.await();

    Assert.assertThat(crawlRequest.getResponseEntity(), Matchers.nullValue());
    Assert.assertNotNull(crawlRequest.getException());

    verify(restTemplate, times(MAX_RETRIES + 1)).exchange(requestEntity, String.class);
  }

  @Test
  public void testDownload_Rate() throws InterruptedException {
    RequestEntity<String> requestEntity = new RequestEntity<>(HttpMethod.GET, URI.create(URL));
    ResponseEntity<String> responseEntity = new ResponseEntity<>("<html>", HttpStatus.OK);

    when(restTemplate.exchange(requestEntity, String.class))
        .thenReturn(responseEntity);

    Stopwatch stopwatch = Stopwatch.createStarted();
    CountDownLatch countDownLatch = new CountDownLatch(QUEUE_SIZE);
    for (int i = 0; i < QUEUE_SIZE; i++) {
      downloader.submit(createCrawlRequest(crawler, URL),
          cr -> countDownLatch.countDown(),
          cr -> countDownLatch.countDown());
    }
    countDownLatch.await();
    stopwatch.stop();

    Assert.assertThat(Math.abs(stopwatch.elapsed().toMillis() - Duration
            .ofMillis((QUEUE_SIZE - 1) * REQUEST_INTERVAL_MILLS).toMillis()),
        Matchers.lessThan(100L));
  }
}