package com.topcoder.productsearch.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.topcoder.productsearch.api.services.UserService;
import com.topcoder.productsearch.api.services.WebSiteService;
import com.topcoder.productsearch.cleaner.service.CleanerService;
import com.topcoder.productsearch.cleaner.service.ValidatePagesService;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.SOTruth;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.SOTruthRepository;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.converter.service.ConverterService;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import com.topcoder.productsearch.crawler.service.CrawlerServiceCreator;
import com.topcoder.productsearch.opt_evaluate.service.SOEvaluateService;
import com.topcoder.productsearch.opt_gen_truth.service.SOGenTruthService;

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
   * search opt truth repository
   */
  @Autowired
  SOTruthRepository soTruthRepository;

  /**
   * the web site service
   */
  @Autowired
  WebSiteService webSiteService;

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
  UserService userService;

  /**
   * search opt evaluate service
   */
  @Autowired
  SOEvaluateService soEvaluateService;


  /**
   * search opt gen truth service
   */
  @Autowired
  SOGenTruthService soGenTruthService;

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
        System.out.println("************************************* Cannot find website where id = " + websiteId + " ****************************");
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
   * get params
   *
   * @param args the CLI args
   * @param key  the key
   * @return the string value
   */
  private String getParams(ApplicationArguments args, String key) {
    List<String> values = args.getOptionValues(key);
    if (values == null) {
      return null;
    }
    return values.get(0);
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
      List<String> passwd = args.getOptionValues("passwd");
      if (passwd == null || passwd.isEmpty()) {
        throw new IllegalArgumentException("Missing value for --passwd option.");
      }
      String[] usernamePassword = passwd.get(0).split(":");
      if (usernamePassword.length < 1) {
        throw new IllegalArgumentException("Invalid value for --passwd option. It should be 'username:password'.");
      }
      this.userService.updatePassword(usernamePassword[0], usernamePassword[1]);

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
    } else if ("opt_evaluate".equalsIgnoreCase(procs.get(0))) {
      String searchWords = getParams(args, "search-words");
      String weightsString = getParams(args, "weights");
      String truthId = getParams(args, "truth");
      if (truthId == null) {
        throw new IllegalArgumentException("parameter truth is required");
      }

      SOTruth soTruth = soTruthRepository.findOne(Integer.valueOf(truthId));
      if (soTruth == null) {
        throw new IllegalArgumentException("cannot find truth where id = " + truthId);
      }
      if (weightsString == null) {
        soEvaluateService.evaluate(soTruth, searchWords, null);
      } else {
        List<Float> weights;
        try {
          weights = Arrays.stream(weightsString.split(",")).map(Float::valueOf)
              .collect(Collectors.toList());
        } catch (Exception e) {
          logger.info(weightsString + " is not an valid int array");
          logger.info("Exiting...");
          return;
        }
        soEvaluateService.evaluate(soTruth, searchWords, weights);
      }
    } else if ("opt_gen_truth".equalsIgnoreCase(procs.get(0))) {
      WebSite site = getSite(args);
      String searchWords = getParams(args, "search-words");
      String searchWordsPath = getParams(args, "search-words-path");
      soGenTruthService.genTruth(site, searchWords, searchWordsPath);
    } else if ("scrape".equalsIgnoreCase(procs.get(0))) {
      // "scrape" task for scraping data from a page specified by the 'url' parameter.
      WebSite site = getSite(args);

      List<String> urls = args.getOptionValues("url");
      if (urls == null || urls.size() ==0 ) {
        logger.error("url is required");
        return;
      }
      logger.info("Start scraping page from: " + urls.get(0));
      CPage page = webSiteService.crawl(site.getId(), urls.get(0));
      if (page != null) {
        logger.info(page.getUrl());
        logger.info(page.getTitle());
        logger.info(page.getContent());
        //logger.info(page.getBody());
      } else {
        logger.info("page is null.");
      }
    } else {
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=converter,--only-data-cleanup");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=converter");
      logger.info("usage : ./gradlew bootRun -Pargs=--proc=converter");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=crawler");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=crawler");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=scrape,--url=...");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=opt_gen_truth,--search-words=\"keyword1 keyword2\"");
      logger.info("usage : ./gradlew bootRun -Pargs=--site=1,--proc=opt_gen_truth,--search-words-path=\"search-words.txt\"");
      logger.info("usage : ./gradlew bootRun -Pargs=--truth=1,--proc=opt_evaluate,--weights=1,2,3,4,5");
      logger.info("usage : ./gradlew bootRun -Pargs=--passwd={username:password}");
    }
  }
}
