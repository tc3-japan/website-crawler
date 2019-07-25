package com.topcoder.productsearch.crawler.service;


import com.topcoder.productsearch.AbstractUnitTest;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.crawler.CrawlerTask;
import com.topcoder.productsearch.crawler.CrawlerThread;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for  CrawlerService
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CrawlerServiceTest extends AbstractUnitTest {


  private WebSite site = createWebSite();
  private CrawlerService crawlerService;

  @Before
  public void init() {
    crawlerService = new CrawlerService(1);
    crawlerService.setMaxDepth(2);
    crawlerService.setMaxRetryTimes(2);
    crawlerService.setSiteTimeLimit(1.f);
    crawlerService.setTaskInterval(1000);
    crawlerService.setTimeout(1.2f);
  }

  @Test
  public void testService() {
    crawlerService.crawler(site);
    assertEquals(crawlerService.getShouldVisit().getOrDefault(site.getUrl(), Boolean.FALSE), Boolean.TRUE);
    assertEquals(crawlerService.getQueueTasks().size(), 0);

    CrawlerThread thread = new CrawlerThread();
    thread.setCrawlerTask(new CrawlerTask(site.getUrl(), site));
    thread.setExpandUrl(new HashSet<>(Arrays.asList(site.getUrl(), "http://google.com")));
    crawlerService.getThreadPoolExecutor().getExecutedHandler().done(thread);
    assertEquals(crawlerService.getShouldVisit().getOrDefault("http://google.com", Boolean.FALSE), Boolean.TRUE);
  }


  @Test
  public void testServiceTimeLimit() {
    crawlerService.setSiteTimeLimit(-1.f);
    crawlerService.crawler(site);
    assertEquals(crawlerService.getQueueTasks().size(), 0);
  }

  @Test
  public void testServiceOutOfQueue() {
    crawlerService.getQueueTasks().add(new CrawlerTask(site.getUrl(), site));
    crawlerService.crawler(site);
    assertEquals(crawlerService.getQueueTasks().size(), 1);
  }

}