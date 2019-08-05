package com.topcoder.productsearch.crawler;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The Crawler Thread Pool Executor
 */
@Getter
@Setter
public class CrawlerThreadPoolExecutor extends ThreadPoolExecutor {

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
   * current running task list
   */
  private List<Runnable> runningList;

  /**
   * Creates a new ThreadPoolExecutor with the given initial parameters
   *
   * @param corePoolSize    the number of threads to keep in the pool, even
   *                        if they are idle, unless {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the
   *                        pool
   * @param keepAliveTime   when the number of threads is greater than
   *                        the core, this is the maximum time that excess idle threads
   *                        will wait for new tasks before terminating.
   * @param unit            the time unit for the {@code keepAliveTime} argument
   * @param workQueue       the queue to use for holding tasks before they are
   *                        executed.  This queue will hold only the {@code Runnable}
   *                        tasks submitted by the {@code execute} method.
   */
  public CrawlerThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                   long keepAliveTime, TimeUnit unit,
                                   BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    runningList = new LinkedList<>();
  }


  /**
   * execute task
   *
   * @param command the task
   */
  @Override
  public void execute(Runnable command) {
    runningCount += 1;
    runningList.add(command);
    super.execute(command);
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
    runningList.remove(r);
    executedHandler.done(r);
  }

  /**
   * get all cost time for site
   *
   * @param siteId the site id
   * @return the cost time
   */
  public long getAllCostTime(Integer siteId) {
    return runningList.stream()
        .map(thread -> (CrawlerThread) thread)
        .filter(t -> t.getCrawlerTask().getSite().getId().equals(siteId) && t.getCrawlerTask().getStartTime() > 0)
        .mapToLong(t -> (System.currentTimeMillis() - t.getCrawlerTask().getStartTime()))
        .sum();
  }

}
