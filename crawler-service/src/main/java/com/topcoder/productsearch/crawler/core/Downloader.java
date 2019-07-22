package com.topcoder.productsearch.crawler.core;

import com.google.common.util.concurrent.RateLimiter;
import com.topcoder.productsearch.common.entity.Page;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * Downloader that handles retries.
 */
@Slf4j
public class Downloader {

  /**
   * Task executor for downloading web pages.
   */
  private final ThreadPoolTaskExecutor taskExecutor;

  /**
   * RestTemplate bean for download web pages.
   */
  private final RestTemplate restTemplate;

  /**
   * Rate limiter to throttle request in a unit of time.
   */
  @SuppressWarnings("UnstableApiUsage")
  private final RateLimiter rateLimiter;

  /**
   * The internal queue for pending download tasks.
   */
  private final BlockingQueue<DownloadTask> pendingDownloadTasks;

  /**
   * The thread responsible for setting the pace(rate) of requests.
   */
  private final Thread pacer;

  /**
   * Constructor that takes a {@code TaskExecutor} argument.
   *
   * @param taskExecutor - the task executor for downloading web pages.
   * @param restTemplate - the rest template for making HTTP requests.
   * @param requestInterval - the interval between subsequent requests, rate = 1/requestInterval.
   * @param queueSize - the size of the pending download task queue.
   */
  public Downloader(ThreadPoolTaskExecutor taskExecutor, RestTemplate restTemplate,
      Duration requestInterval, int queueSize) {
    this.taskExecutor = taskExecutor;
    this.restTemplate = restTemplate;
    this.rateLimiter = getRateLimiter(requestInterval);
    this.pendingDownloadTasks = new ArrayBlockingQueue<>(queueSize);
    this.pacer = new Thread(this::pacerRun);
    this.pacer.setDaemon(true);
    this.pacer.setName("Downloader-Pacer");
  }

  /**
   * Submit a new download task and two callback methods for handling success and failure. The
   * client should check the return value to see if the task was successfully submitted, and
   * re-submit if failed.
   *
   * @param request - the page to download.
   * @param onSuccess - callback for successful download.
   * @param onFailure - callback for download failure.
   * @return true if the task is accepted, false otherwise.
   */
  public boolean submit(CrawlRequest request, Consumer<CrawlRequest> onSuccess,
      Consumer<CrawlRequest> onFailure) {
    DownloadTask downloadTask = new DownloadTask(request, onSuccess, onFailure);
    return pendingDownloadTasks.offer(downloadTask);
  }

  /**
   * Called after the object is constructed.
   */
  @PostConstruct
  public void start() {
    this.pacer.start();
  }

  /**
   * Callback method before ApplicationContext shutdown.
   */
  @PreDestroy
  public void shutdown() {
    taskExecutor.shutdown();
    pacer.interrupt();
  }

  /**
   * Get a rate limiter for the given request interval.
   *
   * @param requestInterval - expected interval between requests.
   * @return a {@code RateLimiter} object whose rate is 1.0/requestInterval.
   */
  private RateLimiter getRateLimiter(Duration requestInterval) {
    double seconds = requestInterval.getSeconds() + requestInterval.getNano() * 1e-9;
    return RateLimiter.create(1.0 / seconds);
  }

  /**
   * The entry point for the pacer thread.
   */
  private void pacerRun() {
    while (!taskExecutor.getThreadPoolExecutor().isShutdown()) {
      try {
        DownloadTask task = pendingDownloadTasks.poll(1, TimeUnit.SECONDS);
        if (task != null) {
          submitTaskNicely(task);
        }
      } catch (InterruptedException e) {
        logger.info("Pacer interrupted, exit.");
        break;
      }
    }
  }

  /**
   * Submit the task to the executor, at the given rate so that we do not initiate download too
   * frequently. Resubmit if task is rejected but pool is not shutdown.
   *
   * @param task - the task to
   */
  private void submitTaskNicely(DownloadTask task) {
    boolean done = false;
    while (!done) {
      rateLimiter.acquire();
      try {
        taskExecutor.submit(task);
        done = true;
      } catch (TaskRejectedException e) {
        if (taskExecutor.getThreadPoolExecutor().isShutdown()) {
          logger.warn("TaskExecutor shut down.");
          done = true;
        }
      }
    }
  }


  /**
   * The download task runnable, that will be scheduled for execution.
   */
  private class DownloadTask implements Runnable {

    /**
     * Page to download.
     */
    private final CrawlRequest crawlRequest;
    /**
     * On success callback.
     */
    private final Consumer<CrawlRequest> onSuccess;
    /**
     * On failure callback.
     */
    private final Consumer<CrawlRequest> onFailure;

    public DownloadTask(CrawlRequest crawlRequest, Consumer<CrawlRequest> onSuccess,
        Consumer<CrawlRequest> onFailure) {
      this.crawlRequest = crawlRequest;
      this.onSuccess = onSuccess;
      this.onFailure = onFailure;
    }

    @Override
    public void run() {
      logger.debug("Downloading page: {}", crawlRequest.getUrl());
      try {
        try {
          // Clear previous response and exception, if any
          crawlRequest.setException(null);
          crawlRequest.setResponseEntity(null);

          RequestEntity<String> requestEntity = new RequestEntity<>(HttpMethod.GET,
              URI.create(crawlRequest.getUrl()));
          Page page = crawlRequest.getExistingPage();
          if (page != null) {
            if (page.getEtag() != null) {
              requestEntity.getHeaders().setIfNoneMatch(page.getEtag());
            }
            if (page.getLastModified() != null) {
              requestEntity.getHeaders().setIfModifiedSince(page.getLastModified().getTime());
            }
          }
          ResponseEntity<String> resp = restTemplate.exchange(requestEntity, String.class);

          logger.debug("Downloaded page: {}", crawlRequest.getUrl());

          crawlRequest.setResponseEntity(resp);

          handleResponse(resp);

        } catch (InterruptedException e) {
          throw e;
        } catch (Exception e) {
          logger.error("Exception while downloading page {}", crawlRequest.getUrl(), e);
          crawlRequest.setException(e);
          if (e.getCause() instanceof SocketTimeoutException) {
            retryDownload();
          } else {
            onFailure.accept(crawlRequest);
          }
        }
      } catch (InterruptedException e) {
        logger.warn("Download interrupted.", e);
      }
    }

    /**
     * Handles the server response.
     * <pre>
     *   - 2xx Successful: call onSuccess
     *   - 304 Unmodified: output log. call onSuccess
     *   - 3xx Redirect: schedule new PageRequest. call onSuccess
     *   - 4xx Client error: output log + dump response body, call onFailure.
     * </pre>
     */
    private void handleResponse(ResponseEntity<String> resp)
        throws InterruptedException {
      if (resp.getStatusCode().is2xxSuccessful()) {
        onSuccess.accept(crawlRequest);
      } else if (resp.getStatusCodeValue() == HttpStatus.NOT_MODIFIED.value()) {
        logger.info("Server responded 304 not modified for page {}", crawlRequest.getUrl());
        onSuccess.accept(crawlRequest);
      } else if (resp.getStatusCode().is3xxRedirection()) {
        onSuccess.accept(crawlRequest);
      } else if (resp.getStatusCode().is4xxClientError()) {
        logError(resp);
        onFailure.accept(crawlRequest);
      } else if (resp.getStatusCode().is5xxServerError()) {
        logError(resp);
        retryDownload();
      }
    }

    /**
     * Schedule the download for retry, if possible, or call onFailure if maximum number of retries
     * reached.
     */
    private void retryDownload() throws InterruptedException {
      if (crawlRequest.retry()) {
        while (!pendingDownloadTasks.offer(this)) {
          Thread.sleep(100);
        }
      } else {
        logger.warn("Maximum number of retries reached, report download failure.");
        onFailure.accept(crawlRequest);
      }
    }

    /**
     * Logs an error for the given response.
     *
     * @param resp - {@code ResponseEntity} to log
     */
    private void logError(ResponseEntity<String> resp) {
      logger.error("Server responded {} {} for page {}. Body: {}", resp.getStatusCode().value(),
          resp.getStatusCode().getReasonPhrase(), crawlRequest.getUrl(), resp.getBody());
    }
  }
}
