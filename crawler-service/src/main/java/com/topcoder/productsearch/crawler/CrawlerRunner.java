package com.topcoder.productsearch.crawler;

import java.util.List;

import com.topcoder.productsearch.crawler.service.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;

/**
 * Crawler Runner, entry for CLI App
 */
//@Component
public class CrawlerRunner implements ApplicationRunner {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(CrawlerRunner.class);

  /**
   * the website database repository
   */
  @Autowired
  WebSiteRepository webSiteRepository;

  /**
   * the crawler service
   */
  @Autowired
  CrawlerService crawlerService;

  /**
   * run cli app
   *
   * @param args the args
   * @throws Exception the run exception
   */
  @Override
  public void run(ApplicationArguments args) throws Exception {

    List<String> sites = args.getOptionValues("site");
    if (sites == null || sites.isEmpty()) {
      throw new IllegalArgumentException("Missing parameter '--site=<site-id>'");
    }
    int siteId = Integer.parseInt(sites.get(0));

    WebSite website = webSiteRepository.findOne(siteId);

    if (website == null) {
      throw new Exception("can not find website where id = " + siteId);
    } else {
      logger.info(">>> Start crawling on : " + website.getName());
      crawlerService.crawler(website);
    }
  }

}
