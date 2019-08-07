package com.topcoder.productsearch.common;

import com.topcoder.productsearch.cleaner.service.CleanerService;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.converter.service.ConverterService;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessLauncher implements ApplicationRunner {


  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(ProcessLauncher.class);

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
   * the cleaner service
   */
  @Autowired
  CleanerService cleanerService;


  /**
   * get site by input args if exist
   *
   * @param args the application CLI args
   * @return the website
   * @throws Exception if any error happened
   */
  private WebSite getSite(ApplicationArguments args) throws Exception {
    List<String> sites = args.getOptionValues("site");
    if (sites != null) {
      Integer websiteId = Integer.parseInt(sites.get(0));
      WebSite website = webSiteRepository.findOne(websiteId);
      if (website == null) {
        throw new Exception("can not find website where id = " + websiteId);
      }
      return website;
    }
    return null;
  }

  /**
   * is only run clean process
   *
   * @param args the application
   * @return the result
   */
  private boolean isOnlyClean(ApplicationArguments args) {
    return args.getOptionNames().contains("only-data-cleanup");
  }

  /**
   * run cli app
   *
   * @param args the args
   * @throws Exception the run exception
   */
  @Override
  public void run(ApplicationArguments args) throws Exception {
    // parameters
    if (args.containsOption("rest")) {
      return;
    }
    
    List<String> procs = args.getOptionValues("proc");
    if (procs == null || procs.isEmpty() || "converter".equalsIgnoreCase(procs.get(0))) {
      WebSite site = getSite(args);
      Integer webSiteId = site == null ? null : site.getId();
      if (isOnlyClean(args)) {
        logger.info("run clean up process ...");
        cleanerService.clean(webSiteId);
      } else {
        logger.info("running converter process ...");
        converterService.convert(webSiteId);
      }
    } else if ("crawler".equalsIgnoreCase(procs.get(0))) {
      WebSite website = getSite(args);
      if (website == null) {
        throw new IllegalArgumentException("Missing parameter '--site=<site-id>'");
      }
      logger.info(">>> Start crawling on : " + website.getName());
      crawlerService.crawler(website);
    } else {
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=converter,--only-data-cleanup");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=converter");
      logger.info("usage : ./gradlew bootRun -Pargs=--proc=converter");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=crawler");
    }
  }
}
