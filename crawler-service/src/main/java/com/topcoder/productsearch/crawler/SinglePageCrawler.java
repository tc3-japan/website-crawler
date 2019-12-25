package com.topcoder.productsearch.crawler;

import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.util.Common;

public class SinglePageCrawler {

  private WebSite webSite;

  public SinglePageCrawler(WebSite webSite) {
    this.webSite = webSite;
  }

  public CPage crawl(String url) {

    if (webSite == null) {
      throw new IllegalStateException("webSite must be specifeid.");
    }

    if (!Common.isMatch(this.webSite, url)) {
      throw new IllegalArgumentException(
          String.format("url is not a product page in %s[%d]: %s", webSite.getName(), webSite.getId(), url));
    }

    final CPage p = new CPage();
    CrawlerThread thread = new CrawlerThread() {
      protected void handlingPage(HtmlPage page) {
        p.setUrl(getCrawlerTask().getUrl());
        p.setSiteId(getCrawlerTask().getSite().getId());
        p.setType("product");
        p.setTitle(page.getTitleText());
        p.setBody(page.getBody().asXml());
        p.setContent(getDomHelper().getContentsByCssSelectors(page,
            getCrawlerTask().getSite().getContentSelector()));
        p.setCategory(getDomHelper().getCategoryByPattern(page.getBody().asXml(),
            getCrawlerTask().getSite().getCategoryExtractionPattern()));
        p.setEtag(page.getWebResponse().getResponseHeaderValue("ETag"));
        p.setLastModified(page.getWebResponse().getResponseHeaderValue("Last-Modified"));
      }
    };
    CrawlerTask task = new CrawlerTask(url, webSite, null);
    task.setStartTime(System.currentTimeMillis());
    thread.setCrawlerTask(task);
    thread.setTimeout((int) (webSite.getTimeoutPageDownload() * 60 * 1000));
    thread.setRetryTimes(webSite.getRetryTimes());
    thread.init();

    try {
      thread.download(new WebRequest(new URL(url)));
      return p;
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Specified url is invalid: " + url, e);
    }
  }
}
