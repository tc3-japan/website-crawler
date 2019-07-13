package com.topcoder.productsearch.crawler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;

@Component
public class CrawlerRunner implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(CrawlerRunner.class);
  
  @Autowired
  WebSiteRepository webSiteRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    
    List<String> sites = args.getOptionValues("site");
    if (sites == null || sites.isEmpty()) {
      throw new IllegalArgumentException("Missing parameter '--site=<site-id>'");
    }
    
    int siteId = Integer.parseInt(sites.get(0));
    
    WebSite website = webSiteRepository.findOne(siteId);
    
    logger.info("Start crawling on: " + website.getName());
  }

}
