package com.topcoder.productsearch.crawler.service;

import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.crawler.CrawlerTask;
import com.topcoder.productsearch.crawler.CrawlerThread;
import com.topcoder.productsearch.crawler.CrawlerThreadPoolExecutor;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * the Crawler Service
 */
@Service
@Setter
@Getter
public class CrawlerService {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

  /**
   * The thread pool executor used to generate audio.
   */
  private CrawlerThreadPoolExecutor threadPoolExecutor;

  /**
   * Interval between each subsequent request (milliseconds)
   */
  @Value("${crawler-settings.interval}")
  private Integer taskInterval;

  /**
   * Timeout for downloading a page (minutes)
   */
  @Value("${crawler-settings.timeout-download}")
  private Float timeout;

  /**
   * Max number of times to retry a single page.
   */
  @Value("${crawler-settings.retry-times}")
  private Integer maxRetryTimes;

  /**
   * pending task queue
   */
  private LinkedBlockingQueue<CrawlerTask> queueTasks;

  /**
   * should visit hash map, avoid put duplicate url into pending queue
   */
  private Map<String, Boolean> shouldVisit;

  /**
   * Create a new instance.
   *
   * @param parallelSize task parallel size
   */
  public CrawlerService(@Value("${crawler-settings.parallel-size}") int parallelSize,
      @Value("${crawler-settings.interval}") int taskInterval) {

    threadPoolExecutor = new CrawlerThreadPoolExecutor(parallelSize, taskInterval);
    // set task completed callback
    threadPoolExecutor.setExecutedHandler(runnable -> {
      CrawlerThread thread = (CrawlerThread) runnable;
      if (thread.getExpandUrl() != null && thread.getExpandUrl().size() > 0) {
        thread.getExpandUrl().forEach(url -> {
          if (shouldVisit.getOrDefault(Common.normalize(url), Boolean.FALSE).equals(Boolean.TRUE)) {
            return;
          }
          CrawlerTask task = new CrawlerTask(url, thread.getCrawlerTask().getSite(), thread.getCrawlerTask().getUrl());
          task.setDepth(thread.getCrawlerTask().getDepth() + 1);
          pushTask(task);
        });
      }
      checkTask();
    });

    queueTasks = new LinkedBlockingQueue<>();
    shouldVisit = new HashMap<>();
  }


  /**
   * crawler entry method
   *
   * @param webSite the website
   */
  public void crawler(WebSite webSite) {
    // set timeout for each thread
    threadPoolExecutor.setKeepAliveTime((long) (timeout * 60 * 1000),TimeUnit.MILLISECONDS);

    CrawlerTask crawlerTask = new CrawlerTask(webSite.getUrl(), webSite, null); // root page
    pushTask(crawlerTask);
    checkTask();
  }

  /**
   * push task into pending queue
   *
   * @param task the task
   */
  private void pushTask(CrawlerTask task) {
    shouldVisit.put(Common.normalize(task.getUrl()), Boolean.TRUE);
    logger.debug("add new task url = " + task.getUrl() + ", depth = " + task.getDepth());
    queueTasks.add(task);
  }

  /**
   * check pending task and execute task if condition met
   */
  private void checkTask() {

    logger.debug(String.format("pending task = %d, and running task = %d", queueTasks.size(),
        threadPoolExecutor.getRunningCount()));

    while (!queueTasks.isEmpty()) {
      CrawlerTask task = queueTasks.poll();

      if (CrawlerThreadPoolExecutor.isReachedTimelimt(task.getSite().getCrawlTimeLimit())) {
        // the elapsed time from the start reaches the time limit for a crawling process
        // stop creating new task and ignore/drop this task
        logger.warn(String.format("the elapsed time from the start reaches the time limit " +
            "for a crawling process, ignore/drop this task %s", task.getUrl()));
        continue;
      }

      if (threadPoolExecutor.getRunningCount() >= threadPoolExecutor.getCorePoolSize()) {
        break;
      }

      // put into thread pool, start download page
      CrawlerThread thread = new CrawlerThread();

      thread.setCrawlerTask(task);
      thread.setTaskInterval(taskInterval);
      thread.setTimeout((int) (timeout * 60 * 1000));
      thread.setRetryTimes(maxRetryTimes);
      thread.setMaxDepth(task.getSite().getCrawlMaxDepth());
      thread.setCrawlerService(this);
      thread.init();

      shouldVisit.put(Common.normalize(thread.getCrawlerTask().getUrl()), Boolean.TRUE);
      // schedule execute task after taskInterval
      threadPoolExecutor.schedule(thread, taskInterval, TimeUnit.MILLISECONDS);
      logger.info("schedule a new task, current running count = " + threadPoolExecutor.getRunningCount()
          + ", total running time(ms) = " + (new Date().getTime() - CrawlerThreadPoolExecutor.startedTime.getTime()));
    }

    int taskSize = threadPoolExecutor.getRunningCount();
    if (taskSize <= 0) {
      threadPoolExecutor.shutdown();
    }
  }
}
