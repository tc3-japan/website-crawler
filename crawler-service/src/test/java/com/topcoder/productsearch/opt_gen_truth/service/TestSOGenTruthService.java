package com.topcoder.productsearch.opt_gen_truth.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.converter.service.SolrService;
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

  @Mock
  SolrService solrService;

  @Mock
  PageRepository pageRepository;

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
    assertEquals(soGenTruthService.unzipRealUrl("https://test.com?a=x"), "https://test.com");
    assertEquals(soGenTruthService.unzipRealUrl("https://test.com/"), "https://test.com");
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
    verify(soTruthDetailRepository, times(3)).save(any(List.class));
    verify(soTruthRepository, times(2)).save(any(SOTruth.class));


    soGenTruthService.setSearchMaxPages(1);
    soGenTruthService.genTruth(site, "setSearchMax", true);
    verify(soTruthDetailRepository, times(5)).save(any(List.class));
    verify(soTruthRepository, times(3)).save(any(SOTruth.class));
  }

  @Test
  public void testGetSimilarityScoresByExplain() {
    String e = "1.8842732 = sum of:\n  1.8842732 = sum of:\n    1.8629308 = max of:\n      0.15585956 = sum of:\n        0.06725858 = weight(html_area2:クルー in 0) [SchemaSimilarity], result of:\n          0.06725858 = score(freq=1.0), product of:\n            0.14660348 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              9 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.4587789 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              1.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              13.0 = dl, length of field\n              13.3 = avgdl, average length of field\n        0.06725858 = weight(html_area2:ネック in 0) [SchemaSimilarity], result of:\n          0.06725858 = score(freq=1.0), product of:\n            0.14660348 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              9 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.4587789 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              1.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              13.0 = dl, length of field\n              13.3 = avgdl, average length of field\n        0.021342402 = weight(html_area2:t in 0) [SchemaSimilarity], result of:\n          0.021342402 = score(freq=1.0), product of:\n            0.046520017 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              10 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.4587789 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              1.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              13.0 = dl, length of field\n              13.3 = avgdl, average length of field\n      0.15395135 = sum of:\n        0.06643512 = weight(html_area1:クルー in 0) [SchemaSimilarity], result of:\n          0.06643512 = score(freq=1.0), product of:\n            0.14660348 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              9 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.45316195 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              1.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              27.0 = dl, length of field\n              26.8 = avgdl, average length of field\n        0.06643512 = weight(html_area1:ネック in 0) [SchemaSimilarity], result of:\n          0.06643512 = score(freq=1.0), product of:\n            0.14660348 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              9 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.45316195 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              1.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              27.0 = dl, length of field\n              26.8 = avgdl, average length of field\n        0.021081101 = weight(html_area1:t in 0) [SchemaSimilarity], result of:\n          0.021081101 = score(freq=1.0), product of:\n            0.046520017 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              10 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.45316195 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              1.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              27.0 = dl, length of field\n              26.8 = avgdl, average length of field\n      0.370729 = sum of:\n        0.12402001 = weight(html_area4:クルー in 0) [SchemaSimilarity], result of:\n          0.12402001 = score(freq=5.0), product of:\n            0.14660348 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              9 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.84595543 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              5.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              3096.0 = dl, length of field (approximate)\n              4564.3 = avgdl, average length of field\n        0.12728801 = weight(html_area4:ネック in 0) [SchemaSimilarity], result of:\n          0.12728801 = score(freq=6.0), product of:\n            0.14660348 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              9 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.86824685 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              6.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              3096.0 = dl, length of field (approximate)\n              4564.3 = avgdl, average length of field\n        0.11942097 = weight(html_area4:t in 0) [SchemaSimilarity], result of:\n          0.11942097 = score(freq=4.0), product of:\n            0.14660348 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              9 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.8145848 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              4.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              3096.0 = dl, length of field (approximate)\n              4564.3 = avgdl, average length of field\n      1.8629308 = sum of:\n        1.068429 = weight(html_area3:クルー in 0) [SchemaSimilarity], result of:\n          1.068429 = score(freq=1.0), product of:\n            1.9924302 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              1 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.5362441 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              1.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              76.0 = dl, length of field (approximate)\n              121.1 = avgdl, average length of field\n        0.7945017 = weight(html_area3:ネック in 0) [SchemaSimilarity], result of:\n          0.7945017 = score(freq=1.0), product of:\n            1.4816046 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n              2 = n, number of documents containing term\n              10 = N, total number of documents with field\n            0.5362441 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n              1.0 = freq, occurrences of term within document\n              1.2 = k1, term saturation parameter\n              0.75 = b, length normalization parameter\n              76.0 = dl, length of field (approximate)\n              121.1 = avgdl, average length of field\n    0.021342402 = max of:\n      0.021342402 = weight(html_area2:men in 0) [SchemaSimilarity], result of:\n        0.021342402 = score(freq=1.0), product of:\n          0.046520017 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n            10 = n, number of documents containing term\n            10 = N, total number of documents with field\n          0.4587789 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n            1.0 = freq, occurrences of term within document\n            1.2 = k1, term saturation parameter\n            0.75 = b, length normalization parameter\n            13.0 = dl, length of field\n            13.3 = avgdl, average length of field\n      0.021081101 = weight(html_area1:men in 0) [SchemaSimilarity], result of:\n        0.021081101 = score(freq=1.0), product of:\n          0.046520017 = idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:\n            10 = n, number of documents containing term\n            10 = N, total number of documents with field\n          0.45316195 = tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:\n            1.0 = freq, occurrences of term within document\n            1.2 = k1, term saturation parameter\n            0.75 = b, length normalization parameter\n            27.0 = dl, length of field\n            26.8 = avgdl, average length of field";
    Map<String,Float> scores = soGenTruthService.getSimilarityScoresByExplain(e);

    assertEquals(scores.get("html_area1"),Float.valueOf(0.17503245f));
    assertEquals(scores.get("html_area2"),Float.valueOf(0.17720196f));
    assertEquals(scores.get("html_area3"),Float.valueOf(1.8629308f));
    assertEquals(scores.get("html_area4"),Float.valueOf(0.370729f));
  }
}
