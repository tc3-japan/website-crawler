package com.topcoder.productsearch.crawler.core;

import java.util.concurrent.PriorityBlockingQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * Responsible for scheduling the download tasks. Also filters out duplicate urls.
 */
@Slf4j
public class Scheduler {

  /**
   * Initial capacity of the underlying priority queue.
   */
  private static final int INITIAL_CAPACITY = 1000;

  /**
   * Deduplication filter for checking if a url has been processed (duplicate).
   */
  private final Deduplicator deduplicator;

  /**
   * Priority blocking queue for picking the highest priority task.
   */
  private final PriorityBlockingQueue<CrawlRequest> taskQueue;

  /**
   * Constructor that takes a {@code Deduplicator} as argument.
   *
   * @param deduplicator - the deduplication filter implementation.
   */
  public Scheduler(Deduplicator deduplicator) {
    this.deduplicator = deduplicator;
    this.taskQueue = new PriorityBlockingQueue<>(INITIAL_CAPACITY, CrawlRequest.COMPARATOR);
  }

  /**
   * Schedule a download task for processing later.
   *
   * @param crawlRequest - the task to schedule.
   */
  public boolean schedule(CrawlRequest crawlRequest) {
    if (deduplicator.hasSeen(crawlRequest.getUrl())) {
      // We have seen this url.
      logger.debug("Skipped duplicate URL: {}", crawlRequest.getUrl());
      return false;
    }

    // PriorityBlockingQueue will never return false.
    taskQueue.offer(crawlRequest);
    return true;
  }

  /**
   * Checks if there are more download tasks in the queue.
   *
   * @return true if the queue is not empty, otherwise false.
   */
  public boolean hasMoreRequests() {
    return !taskQueue.isEmpty();
  }

  /**
   * Get the next download task.
   *
   * @return - the next download task, or null if the queue is empty.
   */
  public CrawlRequest getNextRequest() {
    return taskQueue.poll();
  }
}
