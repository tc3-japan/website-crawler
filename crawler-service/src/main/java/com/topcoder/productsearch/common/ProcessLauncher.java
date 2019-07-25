package com.topcoder.productsearch.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.converter.service.ConverterService;
import com.topcoder.productsearch.crawler.CrawlerRunner;
import com.topcoder.productsearch.crawler.service.CrawlerService;

@Component
public class ProcessLauncher implements ApplicationRunner {


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
   * the converter service
   */
  @Autowired
  ConverterService converterService;

  /**
   * run cli app
   *
   * @param args the args
   * @throws Exception the run exception
   */
  @Override
  public void run(ApplicationArguments args) throws Exception {

    // parameters
    List<String> procs = args.getOptionValues("proc");
    List<String> sites = args.getOptionValues("site");
    
    if (procs == null || procs.isEmpty() || "converter".equalsIgnoreCase(procs.get(0))) {
      // run converter in default or when --site=converter
      // TODO: !!!
      // crawlerService.xxx();
    }
    else if ("crawler".equalsIgnoreCase(procs.get(0))) {
      // run Crawler when --site=crawler
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
}
