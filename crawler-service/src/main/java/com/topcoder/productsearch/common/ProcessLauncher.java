package com.topcoder.productsearch.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.topcoder.productsearch.api.services.AuthService;
import com.topcoder.productsearch.cleaner.service.CleanerService;
import com.topcoder.productsearch.cleaner.service.ValidatePagesService;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.converter.service.ConverterService;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import com.topcoder.productsearch.crawler.service.CrawlerServiceCreator;

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
  CrawlerServiceCreator crawlerServiceCreator;

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
   * the validate pages service
   */
  @Autowired
  ValidatePagesService validatePagesService;

  /**
   * the authentication service
   */
  @Autowired
  AuthService authService;

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
        //throw new Exception("can not find website where id = " + websiteId);
        logger.info("Cannot find website where id = " + websiteId);
        System.out.println("************************************* Cannot find website where id = "+ websiteId +" ****************************");
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
   * is need run converter process
   *
   * @param procs the process name
   * @return the result
   */
  private boolean isConverter(List<String> procs) {
    return procs != null && "converter".equalsIgnoreCase(procs.get(0));
  }

  /**
   * is need run validate pages process
   *
   * @param procs the process name
   * @return the result
   */
  private boolean isValidatePages(List<String> procs) {
    return procs != null && "validate-pages".equalsIgnoreCase(procs.get(0));
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
    if (args.containsOption("passwd")) {
      List<String> pw = args.getOptionValues("passwd");
      if (pw == null || pw.isEmpty()) {
        throw new IllegalArgumentException("Missing value for --passwd option.");
      }
      authService.generatePassword(pw.get(0));
      return;
    }

    List<String> procs = args.getOptionValues("proc");
    if (procs == null || procs.isEmpty()
        || isConverter(procs)
        || isValidatePages(procs)) {
      WebSite webSite = getSite(args);
      // Integer webSiteId = webSite == null ? null : webSite.getId();
      if (isOnlyClean(args)) {
        logger.info("run clean up process ...");
        cleanerService.clean(webSite);
      } else if (isConverter(procs)) {
        logger.info("running converter process ...");
        converterService.convert(webSite);
      } else if (isValidatePages(procs)) {
        logger.info("running validate pages service process ...");
        validatePagesService.validate(webSite);
      }
    } else if ("crawler".equalsIgnoreCase(procs.get(0))) {
      List<String> sites = args.getOptionValues("site");
      if (sites == null || sites.isEmpty()) {
        logger.info("Missing parameter '--site=<site-id>'");
        logger.info("Exiting...");
        return;
      }
      int siteId = Integer.parseInt(sites.get(0));
      CrawlerService crawlerService = crawlerServiceCreator.getCrawlerService(siteId);

      if (crawlerService == null) {
        logger.info("Could not create CrawlerService where website id = " + siteId);
        logger.info("Exiting...");
        System.out.println("************************************* Exiting *****************");
        return;
      }
      logger.info(">>> Start crawling on : " + crawlerService.getWebSite().getName());
      crawlerService.crawler();
    } else {
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=converter,--only-data-cleanup");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=converter");
      logger.info("usage : ./gradlew bootRun -Pargs=--proc=converter");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=crawler");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=validate-pages");
      logger.info("usage : ./gradlew bootRun -Pargs=--passwd={password}");
    }
  }
}
