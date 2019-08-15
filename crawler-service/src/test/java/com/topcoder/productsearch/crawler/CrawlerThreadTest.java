package com.topcoder.productsearch.crawler;


import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.topcoder.productsearch.AbstractUnitTest;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.DestinationURLRepository;
import com.topcoder.productsearch.common.repository.PageRepository;


import com.topcoder.productsearch.common.util.DomHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * unit test for crawler thread
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CrawlerThreadTest extends AbstractUnitTest {

  @Mock
  WebClient webClient;

  @Mock
  HtmlPage htmlPage;

  @Mock
  PageRepository pageRepository;

  @Mock
  WebResponse webResponse;


  @Mock
  HtmlElement htmlElement;

  @Mock
  DestinationURLRepository destinationURLRepository;

  @Mock
  DomHelper domHelper;

  @InjectMocks
  CrawlerThread crawlerThread;


  private WebSite webSite = createWebSite();
  private CPage cPage = createPage();
  private CrawlerTask task = createTask();
  private WebRequest matchedUrl = new WebRequest(new URL("https://www.uniqlo.com/us/en/boys-jersey-easy-" +
      "shorts-416524.html?dwvar_416524_color=COL57&cgid=boys-pants-shorts"));
  private WebRequest rootURL = new WebRequest(new URL(webSite.getUrl()));


  public CrawlerThreadTest() throws MalformedURLException {

  }

  @Before
  public void init() throws IOException {
    when(pageRepository.findByUrl(webSite.getUrl())).thenReturn(cPage);
    when(webResponse.getStatusCode()).thenReturn(200);
    when(htmlPage.getWebResponse()).thenReturn(webResponse);
    when(htmlPage.getBody()).thenReturn(htmlElement);
    when(htmlElement.asXml()).thenReturn("");
    when(htmlPage.getWebResponse()).thenReturn(webResponse);
    when(webClient.getPage(rootURL)).thenReturn(htmlPage);
    when(webClient.getPage(matchedUrl)).thenReturn(htmlPage);

    crawlerThread.setTaskInterval(0);
    crawlerThread.setMaxDepth(2);
    crawlerThread.setCrawlerTask(task);
    crawlerThread.setTimeout(600 * 1000);
    task.setStartTime(System.currentTimeMillis());
    crawlerThread.setRetryTimes(1);
    crawlerThread.init();
    crawlerThread.setWebClient(webClient);
    crawlerThread.setDomHelper(domHelper);
  }

  @Test
  public void testThread() {
    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList("#"));
    crawlerThread.download(rootURL);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);


    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList("https://google.com"));
    crawlerThread.download(rootURL);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);


    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl()));
    crawlerThread.download(rootURL);
    assertEquals(crawlerThread.getExpandUrl().size(), 1);

    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList("/a.html"));
    crawlerThread.download(rootURL);
    assertEquals(crawlerThread.getExpandUrl().size(), 2);

    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl()));
    crawlerThread.getExpandUrl().clear();
    task.setUrl(matchedUrl.getUrl().toString());
    when(pageRepository.findByUrl(matchedUrl.getUrl().toString())).thenReturn(cPage);
    when(destinationURLRepository.findByUrl(webSite.getUrl())).thenReturn(createDestinationURL());
    crawlerThread.download(matchedUrl);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);

    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl() + "/test"));
    crawlerThread.getExpandUrl().clear();
    task.setUrl(matchedUrl.getUrl().toString());
    crawlerThread.download(matchedUrl);
    assertEquals(crawlerThread.getExpandUrl().size(), 1);

    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl()));
    crawlerThread.getExpandUrl().clear();
    task.setUrl(matchedUrl.getUrl().toString());
    when(pageRepository.findByUrl(matchedUrl.getUrl().toString())).thenReturn(null);
    crawlerThread.download(matchedUrl);
    assertEquals(crawlerThread.getExpandUrl().size(), 1);
  }


  @Test
  public void testThreadReachedMaxDepth() {
    crawlerThread.setMaxDepth(1);
    crawlerThread.getExpandUrl().clear();
    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl()));
    crawlerThread.download(rootURL);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);
  }

  @Test
  public void testThreadTimeout() {
    crawlerThread.setTimeout(1);
    try {
      crawlerThread.download(rootURL);
    } catch (Exception e) {
      assertEquals("timeout exception for url " + task.getUrl(), e.getMessage());
    }
    crawlerThread.setTimeout(60 * 1000);
  }

  @Test
  public void test304() {
    when(webResponse.getStatusCode()).thenReturn(304);
    crawlerThread.getExpandUrl().clear();
    crawlerThread.download(rootURL);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);
  }

  @Test
  public void test305() {
    when(webResponse.getStatusCode()).thenReturn(305);
    when(webResponse.getResponseHeaderValue("Location")).thenReturn("http://test.com");
    crawlerThread.getExpandUrl().clear();
    crawlerThread.getCrawlerTask().setStartTime(System.currentTimeMillis());
    crawlerThread.download(rootURL);
    assertEquals(crawlerThread.getExpandUrl().size(), 1);
  }

  @Test
  public void test400() {
    crawlerThread.getExpandUrl().clear();
    when(htmlPage.getTitleText()).thenReturn("title");
    when(webResponse.getStatusCode()).thenReturn(400);
    crawlerThread.setRetryTimes(1);
    crawlerThread.download(rootURL);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);

    when(webResponse.getStatusCode()).thenReturn(500);
    crawlerThread.download(rootURL);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);
  }

  @Test
  public void testTimeout() {
    crawlerThread.getCrawlerTask().setStartTime(0L);
    crawlerThread.download(rootURL);
  }
}