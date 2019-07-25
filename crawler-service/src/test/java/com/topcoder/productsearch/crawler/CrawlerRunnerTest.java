package com.topcoder.productsearch.crawler;


import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * unit test for CrawlerRunner
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CrawlerRunnerTest {


  @Mock
  WebSiteRepository webSiteRepository;


  @Mock
  CrawlerService crawlerService;

  @InjectMocks
  CrawlerRunner crawlerRunner;

  @Test
  public void testRunner() {
    // input
    int siteId = 1;

    DefaultApplicationArguments appArgs = new DefaultApplicationArguments(
        new String[]{
            "--site=" + siteId
        }
    );

    WebSite webSite = new WebSite();
    webSite.setId(siteId);

    when(webSiteRepository.findOne(siteId)).thenReturn(webSite);
    when(webSiteRepository.findOne(2)).thenReturn(null);
    doNothing().when(crawlerService).crawler(webSite);


    try {
      crawlerRunner.run(appArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    verify(webSiteRepository, times(1)).findOne(any(Integer.class));


    DefaultApplicationArguments args = new DefaultApplicationArguments(new String[]{});
    try {
      crawlerRunner.run(args);
    } catch (Exception e) {
      assertEquals("Missing parameter '--site=<site-id>'", e.getMessage());
    }

    DefaultApplicationArguments args2 = new DefaultApplicationArguments(new String[]{"--site"});
    try {
      crawlerRunner.run(args2);
    } catch (Exception e) {
      assertEquals("Missing parameter '--site=<site-id>'", e.getMessage());
    }

    DefaultApplicationArguments args3 = new DefaultApplicationArguments(new String[]{"--site=2"});
    try {
      crawlerRunner.run(args3);
    } catch (Exception e) {
      assertEquals("can not find website where id = 2", e.getMessage());
    }
  }


}