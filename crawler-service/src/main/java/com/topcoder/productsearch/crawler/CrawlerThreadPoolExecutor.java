package com.topcoder.productsearch.crawler;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topcoder.productsearch.common.util.JobDiscoverer;

import lombok.Getter;
import lombok.Setter;

/**
 * The Crawler Thread Pool Executor
 */
@Getter
@Setter
public class CrawlerThreadPoolExecutor extends ScheduledThreadPoolExecutor {


  public static Date startedTime = null;

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
   * minimum interval in executions
   */
  private Integer taskInterval = 0;
  
  /**
   * current running count
   */
  private Integer runningCount = 0;

  /**
   * the time when the last task was executed
   */
  private long lastExecutionTime = 0L;
  
  /**
   * lock
   */
  private final ReentrantLock lock = new ReentrantLock(true);

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
  public CrawlerThreadPoolExecutor(int corePoolSize, int taskInterval) {
    super(corePoolSize);
    this.taskInterval = taskInterval;
    this.crawlerThreads = new ConcurrentHashMap<>();

    if (startedTime == null) {
      startedTime = new Date();
    }
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
    
    if (getTaskInterval() <= 0) {
      return;
    }
    
    try {
      lock.lock();
      long elapsedTimeSinceLastExec = System.currentTimeMillis() - getLastExecutionTime();
      if (elapsedTimeSinceLastExec < getTaskInterval()) {
        try {
          Thread.sleep(getTaskInterval() - elapsedTimeSinceLastExec);
        } catch (InterruptedException e) {
        }
      }
      setLastExecutionTime(System.currentTimeMillis());      
    } finally {
      lock.unlock();
    }
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
   * is the Crawler total run time is reached time limit or not
   * @param timeLimit the website timeLimit
   * @return the result
   */
  public static boolean isReachedTimeLimit(int timeLimit) {
    return new Date().getTime() - startedTime.getTime() >= timeLimit * 1000;
  }
}
