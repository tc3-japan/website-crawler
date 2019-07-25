package com.topcoder.productsearch.crawler.util;


import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.topcoder.productsearch.common.util.DomHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


/**
 * unit test for dom helper class
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class DomHelperTest {


  @Mock
  DomNodeList<DomNode> domNodeList;

  @Mock
  HtmlPage htmlPage;

  @Mock
  DomNode domNode;

  @Mock
  NamedNodeMap namedNodeMap;

  @Mock
  Node node;

  @InjectMocks
  DomHelper domHelper;

  @Test
  public void testDomHelper() {

    when(htmlPage.querySelectorAll("a")).thenReturn(domNodeList);
    when(domNodeList.size()).thenReturn(1);
    when(domNodeList.get(0)).thenReturn(domNode);
    when(domNode.getAttributes()).thenReturn(namedNodeMap);
    when(namedNodeMap.getNamedItem("href")).thenReturn(node);
    when(node.getNodeValue()).thenReturn("http://google.com");

    List<String> urls = domHelper.findAllUrls(htmlPage);
    assertEquals(urls.size(), 1);
    assertEquals(urls.get(0), "http://google.com");
  }


}