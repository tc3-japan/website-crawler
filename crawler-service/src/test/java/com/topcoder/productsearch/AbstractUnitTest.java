package com.topcoder.productsearch;


import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.DestinationURL;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.crawler.CrawlerTask;


/**
 * base unit test class
 */
public abstract class AbstractUnitTest {

  protected WebSite createWebSite() {
    WebSite webSite = new WebSite();
    webSite.setId(1);
    webSite.setUrl("https://www.uniqlo.com/us/en/");
    webSite.setContentUrlPatterns("https://www.uniqlo.com/us/en/[^/]+?.html.*?cgid=.*?$");
    return webSite;
  }

  protected CPage createPage() {
    CPage cPage = new CPage();
    cPage.setId(1);
    return cPage;
  }

  protected CrawlerTask createTask() {
    WebSite webSite = createWebSite();
    CrawlerTask task = new CrawlerTask(webSite.getUrl(), webSite);
    return task;
  }

  protected DestinationURL createDestinationURL() {
    DestinationURL destinationURL = new DestinationURL();
    destinationURL.setId(1);
    destinationURL.setUrl("");
    return destinationURL;
  }
}