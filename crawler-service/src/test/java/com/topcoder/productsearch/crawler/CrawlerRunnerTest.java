package com.topcoder.productsearch.crawler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CrawlerRunnerTest {

  @Test
  public void testRun() throws Exception {
    
    // input
    int siteId = 1;
    
    DefaultApplicationArguments appArgs = new DefaultApplicationArguments(
      new String[] {
          "--site=" + siteId  
      }
    );
    
    WebSite webSite = new WebSite();
    webSite.setId(siteId);
    
    // mock
    WebSiteRepository webSiteRepository = mock(WebSiteRepository.class);
    doReturn(webSite).when(webSiteRepository).findOne(siteId);
    
    // testee
    CrawlerRunner crawler = new CrawlerRunner();
    crawler.webSiteRepository = webSiteRepository;
    
    crawler.run(appArgs);
    
    // verify
    verify(webSiteRepository).findOne(siteId);
    
  }
  
  /*
  public void run(ApplicationArguments args) throws Exception {
    
    List<String> sites = args.getOptionValues("site");
    if (sites == null || sites.isEmpty()) {
      throw new IllegalArgumentException("Missing parameter '--site=<site-id>'");
    }
    
    int siteId = Integer.parseInt(sites.get(0));
    
    WebSite website = webSiteRepository.findOne(siteId);
    
    logger.info("Start crawling on: " + website.getName());
  }
   */

}
