package com.topcoder.productsearch.crawler.service;

import com.topcoder.productsearch.AbstractUnitTest;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.crawler.CrawlerTask;
import com.topcoder.productsearch.crawler.CrawlerThread;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;



/**
 * Unit test for  CrawlerService
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CrawlerServiceTest extends AbstractUnitTest {

  @Mock
  WebSiteRepository webSiteRepository;
  
  @InjectMocks
  CrawlerServiceCreator creator;

  WebSite site = createWebSite();
  
  CrawlerService crawlerService;

  @Before
  public void init() {
    site.setCrawlInterval(1000);
    site.setCrawlTimeLimit(1);
    when(webSiteRepository.findOne(anyInt())).thenReturn(site);

    creator.setMaxRetryTimes(2);
    creator.setTimeout(1.2f);
    creator.setParallelSize(1);
    crawlerService = creator.getCrawlerService(site.getId());

    // crawlerService = new CrawlerService(1, 10) // parallelSize, taskInterval;
 
    // crawlerService.setTaskInterval(1000);
    // crawlerService.setTimeout(1.2f);
  }

  @Test
  public void testService() {
    crawlerService.crawler();
    assertEquals(crawlerService.getShouldVisit().getOrDefault(site.getUrl(), Boolean.FALSE), Boolean.TRUE);
    assertEquals(crawlerService.getQueueTasks().size(), 0);

    CrawlerThread thread = new CrawlerThread();
    thread.setCrawlerTask(new CrawlerTask(site.getUrl(), site, null));
    thread.setExpandUrl(new HashSet<>(Arrays.asList(site.getUrl(), "http://google.com")));
    crawlerService.getThreadPoolExecutor().getExecutedHandler().done(thread);
    assertEquals(crawlerService.getShouldVisit().getOrDefault("http://google.com", Boolean.FALSE), Boolean.TRUE);
  }


  @Test
  public void testServiceTimeLimit() {
    site.setCrawlTimeLimit(-1);
    crawlerService.crawler();
    assertEquals(crawlerService.getQueueTasks().size(), 0);
  }

  @Test
  public void testServiceOutOfQueue() {
    crawlerService.getThreadPoolExecutor().setStartedTime(new Date());
    crawlerService.getThreadPoolExecutor().setRunningCount(1);
    crawlerService.getThreadPoolExecutor().setCorePoolSize(1);
    crawlerService.getQueueTasks().add(new CrawlerTask(site.getUrl(), site, null));
    crawlerService.crawler();
    assertEquals(crawlerService.getQueueTasks().size(), 1);
  }

}