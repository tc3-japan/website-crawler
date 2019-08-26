package com.topcoder.productsearch.crawler;

import com.topcoder.productsearch.common.util.JobDiscoverer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * The Crawler Thread Pool Executor
 */
@Getter
@Setter
public class CrawlerThreadPoolExecutor extends ScheduledThreadPoolExecutor {

  /**
   * completed callback handler
   */
  public interface ExecutedHandler {
    void done(Runnable runnable);
  }

  /**
   * logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(CrawlerThreadPoolExecutor.class);

  /**
   * current running count
   */
  private Integer runningCount = 0;

  /**
   * completed callback
   */
  private ExecutedHandler executedHandler;

  /**
   * current running task map
   */
  private ConcurrentHashMap<Runnable,CrawlerThread> crawlerThreads;

  /**
   * Creates a new ThreadPoolExecutor with the given initial parameters
   *
   * @param corePoolSize    the number of threads to keep in the pool, even
   *                        if they are idle, unless {@code allowCoreThreadTimeOut} is set
   */
  public CrawlerThreadPoolExecutor(int corePoolSize) {
    super(corePoolSize);
    crawlerThreads = new ConcurrentHashMap<>();
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    runningCount += 1;
    return super.schedule(command, delay, unit);
  }

  /**
   * before execute task
   * @param t the thread
   * @param r the runnable
   */
  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    CrawlerThread taskThread = (CrawlerThread) JobDiscoverer.findRealTask(r);
    crawlerThreads.put(r, taskThread);
    super.beforeExecute(t, r);
  }


  /**
   * after execute
   *
   * @param r the task
   * @param t the exception if have
   */
  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);
    runningCount -= 1;
    CrawlerThread thread = crawlerThreads.get(r);
    crawlerThreads.remove(r);
    executedHandler.done(thread);

  }

  /**
   * get all cost time for site
   *
   * @param siteId the site id
   * @return the cost time
   */
  public long getAllCostTime(Integer siteId) {
    return crawlerThreads.values().stream()
        .filter(t -> t.getCrawlerTask().getSite().getId().equals(siteId) && t.getCrawlerTask().getStartTime() > 0)
        .mapToLong(t -> (System.currentTimeMillis() - t.getCrawlerTask().getStartTime()))
        .sum();
  }

}
