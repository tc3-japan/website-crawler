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
   * Time limit for crawling an entire single site (seconds)
   */
  @Value("${crawler-settings.time-limit}")
  private Float siteTimeLimit;

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
   * Max depth that will be allowed to crawl for a site
   */
  @Value("${crawler-settings.max-depth}")
  private Integer maxDepth;

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
  public CrawlerService(@Value("${crawler-settings.parallel-size}") int parallelSize) {

    threadPoolExecutor = new CrawlerThreadPoolExecutor(parallelSize, parallelSize * 2, 0L,
        TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(parallelSize * 2));
    // set task completed callback
    threadPoolExecutor.setExecutedHandler(runnable -> {
      CrawlerThread thread = (CrawlerThread) runnable;
      if (thread.getExpandUrl() != null && thread.getExpandUrl().size() > 0) {
        thread.getExpandUrl().forEach(url -> {
          if (shouldVisit.getOrDefault(Common.normalize(url), Boolean.FALSE).equals(Boolean.TRUE)) {
            return;
          }
          CrawlerTask task = new CrawlerTask(url, thread.getCrawlerTask().getSite());
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

    CrawlerTask crawlerTask = new CrawlerTask(webSite.getUrl(), webSite); // root page
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
    logger.info("add new task url = " + task.getUrl() + ", depth = " + task.getDepth());
    queueTasks.add(task);
  }

  /**
   * check pending task and execute task if condition met
   */
  private void checkTask() {

    logger.info(String.format("pending task = %d, and running task = %d", queueTasks.size(),
        threadPoolExecutor.getRunningCount()));

    while (!queueTasks.isEmpty()) {
      CrawlerTask task = queueTasks.poll();
      long costTime = threadPoolExecutor.getAllCostTime(task.getSite().getId());
      if (costTime > siteTimeLimit * 1000) {
        // the elapsed time from the start reaches the time limit for a crawling process
        // stop creating a new request
        logger.warn(String.format("the elapsed time from the start reaches the time limit for a crawling process," +
                " stop creating a new task, %d > %f",
            costTime, siteTimeLimit * 1000));
        break;
      }

      // put into thread pool, start download page
      CrawlerThread thread = new CrawlerThread();

      thread.setCrawlerTask(task);
      thread.setTaskInterval(taskInterval);

      // add random time, avoid website reject task
      thread.setTimeout((int) (timeout * 60 * 1000 + Math.random() * 1000));
      thread.setRetryTimes(maxRetryTimes);
      thread.setMaxDepth(maxDepth);
      thread.setCrawlerService(this);
      thread.init();

      shouldVisit.put(thread.getCrawlerTask().getUrl(), Boolean.TRUE);
      threadPoolExecutor.execute(thread);
      logger.info("execute a new task, current running count = " + threadPoolExecutor.getRunningCount()
          + ", total running time(ms) = " + costTime);
      if (threadPoolExecutor.getRunningCount() >= threadPoolExecutor.getCorePoolSize()) {
        break;
      }
    }

    int taskSize = threadPoolExecutor.getRunningCount();
    if (taskSize <= 0) {
      threadPoolExecutor.shutdown();
    }
  }
}
