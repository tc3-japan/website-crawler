package com.topcoder.productsearch.common;

import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.converter.service.ConverterService;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProcessLauncher implements ApplicationRunner {

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
      // run converter in default or when --proc=converter
      Integer siteId = null;
      if (sites != null && !sites.isEmpty()) {
        siteId = Integer.parseInt(sites.get(0));
      }
      boolean cleanOnly = args.containsOption("only-data-cleanup");

      converterService.process(siteId, cleanOnly);
    } else if ("crawler".equalsIgnoreCase(procs.get(0))) {
      // run Crawler when --proc=crawler
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
