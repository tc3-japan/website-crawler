package com.topcoder.productsearch.crawler;

import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.crawler.core.CrawlEngine;
import com.topcoder.productsearch.crawler.core.Crawler;
import com.topcoder.productsearch.crawler.website.UniqloWebSiteCrawler;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Crawler runner.
 */
@Slf4j
@Profile("!test")
@Component
public class CrawlerRunner implements ApplicationRunner {

  /**
   * WebSite repository.
   */
  private final WebSiteRepository webSiteRepository;

  /**
   * The crawler engine.
   */
  private final CrawlEngine crawlerEngine;

  @Autowired
  public CrawlerRunner(WebSiteRepository webSiteRepository, CrawlEngine crawlerEngine) {
    this.webSiteRepository = webSiteRepository;
    this.crawlerEngine = crawlerEngine;
  }

  @Override
  public void run(ApplicationArguments args) {

    List<String> sites = args.getOptionValues("site");

    if (sites == null || sites.isEmpty()) {
      throw new IllegalArgumentException("Missing parameter '--site=<site-id>'");
    }

    int siteId = Integer.parseInt(sites.get(0));

    WebSite website = webSiteRepository.findOne(siteId);
    if (website == null) {
      throw new IllegalArgumentException(
          String.format("Could not find website of id = %d", siteId));
    }

    Crawler crawler = getCrawlerForWebSite(website);

    logger.info("Start crawling on: " + website.getName());
    Duration duration = crawlerEngine.run(crawler);
    logger.info("Crawler finished in {} seconds.", duration.getSeconds());
  }

  /**
   * Get a {@code Crawler} instance for the given website.
   *
   * @param website - the website to crawl
   * @return a {@code Crawler} instance.
   */
  private Crawler getCrawlerForWebSite(WebSite website) {
    Crawler crawler;
    switch (website.getName()) {
      case "UNIQLO":
        crawler = new UniqloWebSiteCrawler(website);
        break;
      default:
        throw new IllegalStateException("Unknown website: " + website.getName());
    }
    return crawler;
  }
}
