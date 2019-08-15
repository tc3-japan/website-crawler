package com.topcoder.productsearch.crawler;


import com.topcoder.productsearch.common.entity.WebSite;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * unit test for CrawlerThreadPoolExecutor
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CrawlerThreadPoolExecutorTest {


  @Test
  public void testThreadPool() {
    CrawlerThreadPoolExecutor threadPoolExecutor = new CrawlerThreadPoolExecutor(2, 2 * 2, 0L,
        TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(2 * 2));


    WebSite site = new WebSite();
    site.setId(1);
    site.setUrl("https://www.uniqlo.com/us/en/");
    CrawlerTask task = new CrawlerTask("https://www.uniqlo.com/us/en/", site);

    CrawlerThread thread = new CrawlerThread();
    thread.setTaskInterval(10);
    thread.setCrawlerTask(task);

    threadPoolExecutor.execute(thread);
    assertEquals(threadPoolExecutor.getRunningCount(), Integer.valueOf(1));
    assertEquals(0, threadPoolExecutor.getAllCostTime(1));
    threadPoolExecutor.setExecutedHandler(runnable -> assertEquals(threadPoolExecutor.getRunningCount(),
        Integer.valueOf(0)));
    try {
      sleep(20);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}