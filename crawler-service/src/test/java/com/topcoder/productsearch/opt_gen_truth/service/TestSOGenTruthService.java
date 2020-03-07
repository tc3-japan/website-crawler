package com.topcoder.productsearch.opt_gen_truth.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.topcoder.productsearch.common.entity.SOTruth;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.SOTruthDetailRepository;
import com.topcoder.productsearch.common.repository.SOTruthRepository;
import com.topcoder.productsearch.common.util.Common;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Unit test for SOGenTruthService
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@PrepareForTest(Common.class)
public class TestSOGenTruthService {


  @Mock
  WebClient webClient;

  @Mock
  HtmlPage page;

  @Mock
  SOTruthRepository soTruthRepository;

  @Mock
  SOTruthDetailRepository soTruthDetailRepository;

  @Mock
  HtmlAnchor htmlAnchor;

  @Mock
  DomNodeList<DomNode> domNodeList;

  @Mock
  NamedNodeMap namedNodeMap;

  @Mock
  Node hrefNode;

  @Mock
  DomNode domNode;
  @InjectMocks
  SOGenTruthService soGenTruthService;

  @Before
  public void init() throws IOException {
    when(webClient.getPage(any(URL.class))).thenReturn(page);
    when(page.querySelectorAll(anyString())).thenReturn(domNodeList);
    when(domNodeList.getLength()).thenReturn(2);
    when(domNodeList.get(anyInt())).thenReturn(domNode);
    when(domNode.getAttributes()).thenReturn(namedNodeMap);
    when(namedNodeMap.getNamedItem(anyString())).thenReturn(hrefNode);
    when(domNode.querySelector(anyString())).thenReturn(domNode);
    when(domNode.asText()).thenReturn("string");
    when(hrefNode.getNodeValue()).thenReturn("https://www.uniqlo.com/jp/store/goods/123124");

    when(htmlAnchor.click()).thenReturn(page);
  }

  @Test
  public void testUnzipUrl() {
    assertEquals(soGenTruthService.unzipRealUrl("/url?q=https://test.com/&a=x"), "https://test.com");
    assertEquals(soGenTruthService.unzipRealUrl("/url?q=https://test.com&a=x"), "https://test.com");
    assertNull(soGenTruthService.unzipRealUrl("test"));
  }

  /**
   * test gen truth, note: this need request real network
   *
   * @throws Exception if any error happened
   */
  @Test
  public void testGenTruth() throws Exception {
    WebSite site = new WebSite();
    soGenTruthService.setNumberOfUrl(5);
    soGenTruthService.setSearchMaxPages(2);
    soGenTruthService.setWebClient(Common.createWebClient());
    soGenTruthService.setWebClient(webClient);
    site.setId(1);
    site.setContentUrlPatterns("https://www.uniqlo.com/jp/store/goods/[\\d\\-]+");
    site.setGoogleParam("+site:https://www.uniqlo.com/jp/");
    site.setSupportsJs(false);
    soGenTruthService.genTruth(site, "クルーネックT MEN", false);
    verify(soTruthDetailRepository, times(1)).save(any(List.class));
    verify(soTruthRepository, times(1)).save(any(SOTruth.class));

    when(page.getByXPath(anyString())).thenReturn(Collections.singletonList(htmlAnchor));
    soGenTruthService.setSearchMaxPages(20);
    site.setGoogleParam(null);
    soGenTruthService.genTruth(site, "setSearchMax", true);
    verify(soTruthDetailRepository, times(2)).save(any(List.class));
    verify(soTruthRepository, times(2)).save(any(SOTruth.class));


    soGenTruthService.setSearchMaxPages(1);
    soGenTruthService.genTruth(site, "setSearchMax", true);
    verify(soTruthDetailRepository, times(3)).save(any(List.class));
    verify(soTruthRepository, times(3)).save(any(SOTruth.class));
  }
}
