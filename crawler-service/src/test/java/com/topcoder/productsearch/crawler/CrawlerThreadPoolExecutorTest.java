package com.topcoder.productsearch.crawler;


import com.topcoder.productsearch.common.entity.WebSite;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    CrawlerThreadPoolExecutor threadPoolExecutor = new CrawlerThreadPoolExecutor(2, 10);


    WebSite site = new WebSite();
    site.setId(1);
    site.setUrl("https://www.uniqlo.com/us/en/");
    CrawlerTask task = new CrawlerTask("https://www.uniqlo.com/us/en/", site, null);

    CrawlerThread thread = new CrawlerThread();
    thread.setTaskInterval(10);
    thread.setCrawlerTask(task);

    threadPoolExecutor.schedule(thread, 0, TimeUnit.MILLISECONDS);
    assertEquals(threadPoolExecutor.getRunningCount(), Integer.valueOf(1));
    assertTrue(threadPoolExecutor.isReachedTimeLimit(0));
    threadPoolExecutor.setExecutedHandler(runnable -> assertEquals(threadPoolExecutor.getRunningCount(),
        Integer.valueOf(0)));
    try {
      sleep(20);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}