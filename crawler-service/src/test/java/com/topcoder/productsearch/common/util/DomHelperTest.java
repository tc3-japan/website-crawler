package com.topcoder.productsearch.common.util;


import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


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

  @Mock
  HtmlPage page;

  @Mock
  HtmlElement body;

  @Mock
  DomNodeList<DomNode> domNodes;

  @Mock
  WebClient webClient;

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

  @Test
  public void testGetContentsByCssSelectors(){

    when(page.getBody()).thenReturn(body);
    when(body.asXml()).thenReturn("<body></body>");

    assertEquals("<body></body>", domHelper.getContentsByCssSelectors(page, null));
    assertEquals("<body></body>", domHelper.getContentsByCssSelectors(page, " "));

    when(domNodes.size()).thenReturn(1);
    when(domNodes.get(anyInt())).thenReturn(domNode);

    when(domNode.asXml()).thenReturn("<div>text</div>");
    when(page.querySelectorAll(anyString())).thenReturn(domNodes);

    when(page.getWebClient()).thenReturn(webClient);

    assertEquals(true, domHelper.getContentsByCssSelectors(page, "product-info\ntest").startsWith("<content selector=\"product-info\">"));
  }

  @Test
  public void testGetCategoryByPattern(){
    assertEquals("name",domHelper.getCategoryByPattern("your name?","your (\\w++)?"));
    assertEquals("",domHelper.getCategoryByPattern("your name?","your ([abc]++)?"));
  }


  @Test
  public void testGetHtmlAreas() {
    String contents = "<content>a</content><content>b</content>";
    List<String> areas = domHelper.getHtmlAreasFromContents(contents);

    assertEquals(areas.size(), 2);
    assertEquals(areas.get(0), "a");
    assertEquals(areas.get(1), "b");
  }

}