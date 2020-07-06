package com.topcoder.productsearch.crawler;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.topcoder.productsearch.AbstractUnitTest;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.DestinationURL;
import com.topcoder.productsearch.common.entity.SourceURL;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.DestinationURLRepository;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.repository.SourceURLRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.common.util.DomHelper;
import com.topcoder.productsearch.crawler.service.CrawlerService;

/**
 * unit test for crawler thread
 */
@ActiveProfiles("test")
// @RunWith(SpringJUnit4ClassRunner.class)
// @SpringBootTest
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PrepareForTest({Common.class})
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
  SourceURLRepository sourceURLRepository;

  @Mock
  DomHelper domHelper;

  @Mock
  CrawlerService crawlerService;

  @Mock
  CrawlerThreadPoolExecutor threadPoolExecutor;


  @InjectMocks
  CrawlerThread crawlerThread;


  private WebSite webSite = createWebSite();
  private CPage cPage = createPage();
  private CrawlerTask task = createTask();
  private WebRequest matchedUrlWebRequest = new WebRequest(new URL("https://www.uniqlo.com/us/en/boys-jersey-easy-" +
      "shorts-416524.html?dwvar_416524_color=COL57&cgid=boys-pants-shorts"));
  private WebRequest rootURLWebRequest = new WebRequest(new URL(webSite.getUrl()));





  public CrawlerThreadTest() throws MalformedURLException {
    task.setSite(webSite);
  }

  @Before
  public void init() throws IOException {
    when(pageRepository.findByUrl(webSite.getUrl())).thenReturn(cPage);
    when(webResponse.getStatusCode()).thenReturn(200);
    when(htmlPage.getWebResponse()).thenReturn(webResponse);
    when(htmlPage.getBody()).thenReturn(htmlElement);
    when(htmlElement.asXml()).thenReturn("");
    when(htmlPage.getWebResponse()).thenReturn(webResponse);
    when(webClient.getPage(rootURLWebRequest)).thenReturn(htmlPage);
    when(webClient.getPage(matchedUrlWebRequest)).thenReturn(htmlPage);
    when(webClient.getOptions()).thenReturn(new WebClientOptions());
    when(crawlerService.getThreadPoolExecutor()).thenReturn(threadPoolExecutor);
    when(threadPoolExecutor.hasReachedTimeLimit(anyInt())).thenReturn(false);
    PowerMockito.spy(Common.class);
    PowerMockito.doReturn(true).when(Common.class);
    Common.hasAccess(eq(webSite) , anyString());

    crawlerThread.setTaskInterval(0);
    crawlerThread.setMaxDepth(2);
    crawlerThread.setCrawlerTask(task);
    crawlerThread.setTimeout(600 * 1000);
    task.setStartTime(System.currentTimeMillis());
    crawlerThread.setRetryTimes(1);
    crawlerThread.init();
    crawlerThread.setWebClient(webClient);
    crawlerThread.setDomHelper(domHelper);

    // crawlerThread.setCrawlerService(new CrawlerService(10,1000));
    crawlerThread.getCrawlerService().getThreadPoolExecutor().setStartedTime(new Date());
  }

  @Test
  public void testThread() {
    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList("#"));
    crawlerThread.download(rootURLWebRequest);
    assertEquals(0, crawlerThread.getExpandUrl().size());


    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList("https://google.com"));
    crawlerThread.download(rootURLWebRequest);
    assertEquals(0, crawlerThread.getExpandUrl().size());


    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl()+"test.html"));
    crawlerThread.download(rootURLWebRequest);
    assertEquals(1, crawlerThread.getExpandUrl().size());

    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections
        .singletonList(webSite.getUrl() + "men-u-crew-neck-short-sleeve-t-shirt-414351.html?" +
            "dwvar_414351_color=COL46&cgid=men-wear-to-work"));
    crawlerThread.download(rootURLWebRequest);
    assertEquals(2, crawlerThread.getExpandUrl().size());

    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList("/us/en/orders"));
    crawlerThread.download(rootURLWebRequest);
    assertEquals(2, crawlerThread.getExpandUrl().size());

    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList("/us/en/a.pdf"));
    crawlerThread.download(rootURLWebRequest);
    assertEquals(2, crawlerThread.getExpandUrl().size());

    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl()+ "boys-jersey-easy-" +
      "shorts-416524.html?dwvar_416524_color=COL57&cgid=boys-pants-shorts"));
    crawlerThread.getExpandUrl().clear();
    task.setUrl(matchedUrlWebRequest.getUrl().toString());
    task.setSourceUrl("http://test.com");
    when(pageRepository.findByUrl(Common.normalize(matchedUrlWebRequest.getUrl().toString()))).thenReturn(cPage);

    List<DestinationURL> destinationURLs = new ArrayList<DestinationURL>(1);
    destinationURLs.add(createDestinationURL());
    when(destinationURLRepository.findByUrlAndPageId(webSite.getUrl(), 1)).thenReturn(destinationURLs);

    when(sourceURLRepository.save(any(SourceURL.class))).thenReturn(new SourceURL());
    crawlerThread.download(matchedUrlWebRequest);
    assertEquals(1, crawlerThread.getExpandUrl().size());
    verify(sourceURLRepository, times(1)).findByUrlAndPageId(any(String.class), any(Integer.class));
    verify(sourceURLRepository, times(1)).save(any(SourceURL.class));


    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl() + "/test.html"));
    crawlerThread.getExpandUrl().clear();
    when(sourceURLRepository.findByUrlAndPageId(task.getSourceUrl(), 1)).thenReturn(null);
    task.setUrl(matchedUrlWebRequest.getUrl().toString());
    crawlerThread.download(matchedUrlWebRequest);
    assertEquals( 1, crawlerThread.getExpandUrl().size());
    verify(sourceURLRepository, times(2)).findByUrlAndPageId(any(String.class), any(Integer.class));
    verify(sourceURLRepository, times(2)).save(any(SourceURL.class));

    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl()+"/test.html"));
    crawlerThread.getExpandUrl().clear();
    task.setUrl(matchedUrlWebRequest.getUrl().toString());
    when(pageRepository.findByUrl(matchedUrlWebRequest.getUrl().toString())).thenReturn(null);
    crawlerThread.download(matchedUrlWebRequest);
    assertEquals( 1, crawlerThread.getExpandUrl().size());
  }


  @Test
  public void testThreadReachedMaxDepth() {
    crawlerThread.setMaxDepth(1);
    crawlerThread.getExpandUrl().clear();
    when(domHelper.findAllUrls(htmlPage)).thenReturn(Collections.singletonList(webSite.getUrl()));
    crawlerThread.download(rootURLWebRequest);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);
  }

  @Test
  public void testThreadTimeout() {
    crawlerThread.setTimeout(1);
    try {
      crawlerThread.download(rootURLWebRequest);
    } catch (Exception e) {
      assertEquals("timeout exception for url " + task.getUrl(), e.getMessage());
    }
    crawlerThread.setTimeout(60 * 1000);
  }

  @Test
  public void test304() {
    when(webResponse.getStatusCode()).thenReturn(304);
    crawlerThread.getExpandUrl().clear();
    crawlerThread.download(rootURLWebRequest);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);
  }

  @Test
  public void test305() {
    when(webResponse.getStatusCode()).thenReturn(305);
    when(webResponse.getResponseHeaderValue("Location")).thenReturn("http://test.com");
    crawlerThread.getExpandUrl().clear();
    crawlerThread.getCrawlerTask().setStartTime(System.currentTimeMillis());
    crawlerThread.download(rootURLWebRequest);
    assertEquals(crawlerThread.getExpandUrl().size(), 1);
  }

  @Test
  public void test400() {
    crawlerThread.getExpandUrl().clear();
    when(htmlPage.getTitleText()).thenReturn("title");
    when(webResponse.getStatusCode()).thenReturn(400);
    crawlerThread.setRetryTimes(1);
    crawlerThread.download(rootURLWebRequest);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);

    when(webResponse.getStatusCode()).thenReturn(500);
    crawlerThread.download(rootURLWebRequest);
    assertEquals(crawlerThread.getExpandUrl().size(), 0);
  }

  @Test
  public void test501() {
    crawlerThread.getExpandUrl().clear();
    when(htmlPage.getTitleText()).thenReturn("title");
    when(webResponse.getStatusCode()).thenReturn(501);
    crawlerThread.setRetryTimes(1);
    crawlerThread.download(rootURLWebRequest);
    assertEquals(crawlerThread.getExpandUrl().size(),0);
  }

  @Test
  public void testTimeout() {
    crawlerThread.getCrawlerTask().setStartTime(0L);
    crawlerThread.download(rootURLWebRequest);
  }
}