package com.topcoder.productsearch.crawler.core;

import com.topcoder.productsearch.common.entity.Page;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;

/**
 * The crawler engine is responsible for coordinating the download, parse and save operations.
 * <p>
 * Usage:
 * </p>
 * <pre>
 *   // Instantiate a crawler
 *   Crawler crawler = ...;
 *   // Call run on the crawler, and wait.
 *   crawlEngine.run(crawler)
 * </pre>
 */
@Slf4j
public class CrawlEngine implements CrawlContext {

  /**
   * Settings for the crawling process.
   */
  private final Settings settings;

  /**
   * The download task queue.
   */
  private final Downloader downloader;

  /**
   * The scheduler of download tasks.
   */
  private final Scheduler scheduler;

  /**
   * The task scheduler for running page body processing tasks.
   */
  private final TaskExecutor bodyProcessingTaskExecutor;

  /**
   * The service bean for interaction with database.
   */
  private final CrawlerService crawlerService;

  /**
   * Pending request set. Requests are added to this set when they are successfully put into the
   * scheduler, and removed when the downloaded content is processed or the request failed.
   */
  private final Set<CrawlRequest> pendingRequests = Collections.synchronizedSet(new HashSet<>());

  /**
   * Construct a new {@code CrawlerEngine} instance.
   *
   * @param settings - the settings
   * @param downloader - the page downloader
   * @param scheduler - the page download scheduler.
   * @param bodyProcessingTaskExecutor - task executor for running the body processing tasks
   */
  public CrawlEngine(
      Settings settings,
      Downloader downloader,
      Scheduler scheduler,
      TaskExecutor bodyProcessingTaskExecutor,
      CrawlerService crawlerService) {
    this.settings = settings;
    this.downloader = downloader;
    this.scheduler = scheduler;
    this.bodyProcessingTaskExecutor = bodyProcessingTaskExecutor;
    this.crawlerService = crawlerService;
  }

  @Override
  public void save(Page page, Collection<String> destinationUrls) {
    logger.debug("Saving Page {} and destination urls {} to database.", page, destinationUrls);
    crawlerService.save(page, destinationUrls);
  }

  /**
   * Entry point of the crawl engine.
   * <ol>
   * <li>Schedule the start urls for download</li>
   * <li>While there's pending crawl requests, poll scheduler
   * and submit request to downloader.</li>
   * <li>Sleep for a while.</li>
   * </ol>
   *
   * @param crawler - the crawler implementation.
   * @return time taken to fully crawl the website.
   */
  public Duration run(Crawler crawler) {
    // Schedule to download the start url collection.
    crawler.startTimer(settings.getSiteTimeLimitSeconds());
    scheduleForDownload(crawler, crawler.getStartUrls(), 0);

    try {
      // While there's still pending requests.
      while (hasPendingRequests()) {
        drainScheduler();
        Thread.sleep(settings.getRequestInterval().toMillis());
      }
    } catch (InterruptedException e) {
      logger.warn("Interrupted.", e);
    }

    return crawler.stopTimer();
  }

  /**
   * Schedule a collection of urls for download. Ignore if the depth is greater than max crawl
   * depth.
   *
   * @param crawler - crawler that initiated the download
   * @param urls - collection of urls to download
   * @param depth - the depth of the urls
   */
  @Override
  public void scheduleForDownload(Crawler crawler, Collection<String> urls, int depth) {
    if (depth < settings.getMaxCrawlDepth()) {
      for (String url : urls) {
        CrawlRequest request = createRequest(crawler, url, depth);
        if (scheduler.schedule(request)) {
          pendingRequests.add(request);
        }
      }
    } else {
      logger
          .info("Skipping pages: {}, maximum depth reached: {}", urls, settings.getMaxCrawlDepth());
    }
  }

  /**
   * Poll scheduler for the next request and submit it for download until there's no more requests
   * in the scheduler.
   *
   * @throws InterruptedException if thread is interrupted.
   */
  private void drainScheduler() throws InterruptedException {
    while (scheduler.hasMoreRequests()) {
      CrawlRequest crawlRequest = scheduler.getNextRequest();

      // Populate request with existing Page entity
      Page existingPage = crawlerService.findByUrl(crawlRequest.getUrl());
      if (existingPage != null) {
        logger.debug("Found existing page entity in database: {}", existingPage);
        crawlRequest.setExistingPage(existingPage);
      }

      // Submit for download, may need to wait if download queue is full.
      queueForDownload(crawlRequest);
    }
  }

  /**
   * Submit request for download, block until there's position in the download queue.
   *
   * @param crawlRequest - the request to download
   * @throws InterruptedException - if interrupted.
   */
  private void queueForDownload(CrawlRequest crawlRequest) throws InterruptedException {
    while (!downloader.submit(crawlRequest,
        this::onPageDownloadSuccess,
        this::onPageDownloadFailure)) {
      Thread.sleep(settings.getRequestInterval().toMillis());
    }
    logger.debug("Queued {} for download.", crawlRequest.getUrl());
  }

  /**
   * Checks if there's pending requests.
   *
   * @return true if the pending request set is not empty.
   */
  private boolean hasPendingRequests() {
    return !pendingRequests.isEmpty();
  }

  /**
   * Create a new page download for the given url.
   *
   * @param crawler - crawler that initiated the download
   * @param uri - uri to download
   * @param depth - depth of the url.
   * @return - a {@code DownloadTask} instance
   */
  private CrawlRequest createRequest(Crawler crawler, String uri, int depth) {
    return new CrawlRequest(crawler, uri, depth, settings.getPageDownloadMaxRetry(),
        settings.getPageDownloadRetryInterval(), -depth);
  }

  /**
   * Callback method when a page was successfully downloaded.
   *
   * @param crawlRequest - the crawl request with the response.
   */
  private void onPageDownloadSuccess(CrawlRequest crawlRequest) {
    bodyProcessingTaskExecutor.execute(() -> {
      try {
        crawlRequest.process(this);
      } finally {
        pendingRequests.remove(crawlRequest);
      }
    });
  }

  /**
   * Callback method when a page download failed.
   *
   * @param crawlRequest - the crawl request that failed.
   */
  private void onPageDownloadFailure(CrawlRequest crawlRequest) {
    bodyProcessingTaskExecutor.execute(() -> pendingRequests.remove(crawlRequest));
  }

}
