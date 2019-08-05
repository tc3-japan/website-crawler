package com.topcoder.productsearch;


import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.DestinationURL;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.crawler.CrawlerTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * base unit test class
 */
public abstract class AbstractUnitTest {

  protected List<WebSite> createWebSites(int n, Date lastProcessedAt, Date lastCleanedUpAt) {
    List<WebSite> webSites = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      WebSite webSite = new WebSite();
      webSite.setId(i);
      webSite.setName("website-" + i);
      webSite.setUrl("https://website-" + i + ".com");
      webSite.setCreatedAt(new Date());
      webSite.setLastModifiedAt(new Date());
      if (lastCleanedUpAt != null) {
        webSite.setLastCleanedUpAt(new Date(lastCleanedUpAt.getTime() + 10_000 * i));
      }
      if (lastProcessedAt != null) {
        webSite.setLastProcessedAt(new Date(lastProcessedAt.getTime() + 10_000 * i));
      }
      webSites.add(webSite);
    }
    return webSites;
  }

  protected List<CPage> createPages(int n, int siteId, boolean deleted) {
    List<CPage> cPages = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      CPage cPage = new CPage();
      cPage.setId(i);
      cPage.setSiteId(siteId);
      cPage.setDeleted(deleted);
      cPage.setUrl("https://website-" + siteId + ".com/page-" + i);
      cPages.add(cPage);
    }
    return cPages;
  }

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