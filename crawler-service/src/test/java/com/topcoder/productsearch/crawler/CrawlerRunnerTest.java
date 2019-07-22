package com.topcoder.productsearch.crawler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.crawler.core.CrawlEngine;
import com.topcoder.productsearch.crawler.core.Crawler;
import com.topcoder.productsearch.crawler.website.UniqloWebSiteCrawler;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.boot.DefaultApplicationArguments;

/**
 * Unit tests for {@code CrawlerRunner}.
 */
public class CrawlerRunnerTest extends BaseTest {

  /**
   * Mock website repository
   */
  @Mock
  private WebSiteRepository webSiteRepository;

  /**
   * Mock crawl engine
   */
  @Mock
  private CrawlEngine crawlEngine;

  /**
   * The {@code CrawlerRunner} object under test.
   */
  private CrawlerRunner crawlerRunner;

  /**
   * Site id passed to app arguments
   */
  private int siteId = 1;

  /**
   * App args
   */
  private DefaultApplicationArguments appArgs = new DefaultApplicationArguments(
      new String[]{
          "--site=" + siteId
      }
  );

  @Before
  public void setUp() {
    crawlerRunner = new CrawlerRunner(webSiteRepository, crawlEngine);
  }

  /**
   * Test normal flow of the crawler runner.
   */
  @Test
  public void testRun_Normal() {
    WebSite webSite = new WebSite();
    webSite.setName("UNIQLO");
    webSite.setUrl("https://www.uniqlo.com/us/en/home/");
    webSite.setContentUrlPatterns("https://www.uniqlo.com/us/en/[^/]+?.html.*?cgid=.*?$");
    webSite.setId(siteId);

    // Mock
    doReturn(webSite).when(webSiteRepository).findOne(siteId);
    when(crawlEngine.run(any(Crawler.class))).thenReturn(Duration.ofMillis(1));

    // Run code being tested
    crawlerRunner.run(appArgs);

    // Verify
    verify(webSiteRepository).findOne(siteId);
    verify(crawlEngine).run(Matchers.any(UniqloWebSiteCrawler.class));
  }

  /**
   * Runner should throw IllegalArgumentException when site id is not found.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRun_WebSiteNotFound() {
    // Mock
    doReturn(null).when(webSiteRepository).findOne(siteId);

    try {
      // Run the code being tested
      crawlerRunner.run(appArgs);
    } catch (IllegalArgumentException e) {

      // Verify
      verify(webSiteRepository, times(1)).findOne(siteId);
      verifyZeroInteractions(crawlEngine);

      throw e;
    }
  }

  /**
   * Runner should throw IllegalStateException when site is found but no crawler was defined.
   */
  @Test(expected = IllegalStateException.class)
  public void testRun_WebSiteCrawlerNotFound() {
    WebSite webSite = new WebSite();
    webSite.setName("IKEA");
    webSite.setContentUrlPatterns("https://www.ikea.com/us/en/.*$");
    webSite.setId(siteId);

    // Mock
    doReturn(webSite).when(webSiteRepository).findOne(siteId);

    try {
      // Run the code being tested, passing null for CrawlerEngine
      crawlerRunner.run(appArgs);
    } catch (IllegalStateException e) {

      // Verify
      verify(webSiteRepository, times(1)).findOne(siteId);
      verifyZeroInteractions(crawlEngine);

      throw e;
    }
  }
}
