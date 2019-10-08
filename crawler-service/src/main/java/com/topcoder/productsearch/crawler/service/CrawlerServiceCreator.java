package com.topcoder.productsearch.crawler.service;

import com.topcoder.productsearch.common.entity.URLNormalizers;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.URLNormalizersRepository;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.crawler.CrawlerTask;
import com.topcoder.productsearch.crawler.CrawlerThread;
import com.topcoder.productsearch.crawler.CrawlerThreadPoolExecutor;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the Crawler Service Creator
 */
@Service
@Setter
@Getter
public class CrawlerServiceCreator {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(CrawlerServiceCreator.class);



  /**
   * the website database repository
   */
  @Autowired
  private WebSiteRepository webSiteRepository;

 
  /**
   * url_normalizers table repository
   */
  @Autowired
  private URLNormalizersRepository urlNormalizersRepository;
  
  
  public CrawlerService getCrawlerService(int siteId) {
    WebSite webSite = webSiteRepository.findOne(siteId);
    if (webSite == null) {
      return null;
    }
    return new CrawlerServiceImpl(webSite);

  }

  @Getter
  private class CrawlerServiceImpl implements CrawlerService {

    /**
     * The thread pool executor
     */
    private CrawlerThreadPoolExecutor threadPoolExecutor;
    /**
     * should visit hash map, avoid put duplicate url into pending queue
     */
    private Map<String, Boolean> shouldVisit;
    /**
     * pending task queue
     */
    private LinkedBlockingQueue<CrawlerTask> queueTasks;

    WebSite webSite;
    URLNormalizers urlNormalizers;
    final Pattern pattern ;

    public CrawlerServiceImpl(WebSite webSite)  {

      this.webSite = webSite;
      urlNormalizers = urlNormalizersRepository.findByWebsiteId(webSite.getId());
      pattern = Pattern.compile(urlNormalizers.getRegexPattern());
      threadPoolExecutor = new CrawlerThreadPoolExecutor(webSite.getParallelSize(), webSite.getCrawlInterval());
      // set task completed callback
      threadPoolExecutor.setExecutedHandler(runnable -> {
        CrawlerThread thread = (CrawlerThread) runnable;
        if (thread.getExpandUrl() != null && thread.getExpandUrl().size() > 0) {
          thread.getExpandUrl().forEach(url -> {
            if (shouldVisit.getOrDefault(Common.normalize(url,pattern,urlNormalizers.getSubstitution()), Boolean.FALSE).equals(Boolean.TRUE)) {
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
     */
    public void crawler() {
      // set timeout for each thread
      threadPoolExecutor.setKeepAliveTime((long) (webSite.getTimeoutPageDownload() * 60 * 1000L), TimeUnit.MILLISECONDS);

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
      shouldVisit.put(Common.normalize(task.getUrl(), pattern, urlNormalizers.getSubstitution() ), Boolean.TRUE);
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

        if (threadPoolExecutor.hasReachedTimeLimit(task.getSite().getCrawlTimeLimit())) {
          // the elapsed time from the start reaches the time limit for a crawling process
          // stop creating new task and ignore/drop this task
          logger.warn(String.format("the elapsed time from the start reaches the time limit "
              + "for a crawling process, ignore/drop this task %s", task.getUrl()));
          continue;
        }

        if (threadPoolExecutor.getRunningCount() >= threadPoolExecutor.getCorePoolSize()) {
          break;
        }

        // put into thread pool, start download page
        CrawlerThread thread = new CrawlerThread();

        thread.setCrawlerTask(task);
        thread.setTaskInterval(webSite.getCrawlInterval());
        thread.setTimeout((int) (webSite.getTimeoutPageDownload() * 60 * 1000));
        thread.setRetryTimes(webSite.getRetryTimes());
        thread.setMaxDepth(task.getSite().getCrawlMaxDepth());
        thread.setCrawlerService(this);
        thread.init();

        shouldVisit.put(Common.normalize(thread.getCrawlerTask().getUrl(),pattern,urlNormalizers.getSubstitution()), Boolean.TRUE);
        // schedule execute task after taskInterval
        threadPoolExecutor.schedule(thread, webSite.getCrawlInterval(), TimeUnit.MILLISECONDS);
        logger.info("schedule a new task, current running count = " + threadPoolExecutor.getRunningCount()
            + ", total running time(ms) = " + (new Date().getTime() - threadPoolExecutor.getStartedTime().getTime()));
      }

      int taskSize = threadPoolExecutor.getRunningCount();
      if (taskSize <= 0) {
        threadPoolExecutor.shutdown();
      }
    }

    public CrawlerThreadPoolExecutor getThreadPoolExecutor() {
      return threadPoolExecutor;
    }

  }

}