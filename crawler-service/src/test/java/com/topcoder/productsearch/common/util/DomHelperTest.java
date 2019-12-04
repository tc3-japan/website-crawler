package com.topcoder.productsearch.common.util;


import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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

  @Mock
  HtmlPage page;

  @Mock
  HtmlElement body;

  @Mock
  DomNodeList<DomNode> domNodes;

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

    assertEquals(true, domHelper.getContentsByCssSelectors(page, "product-info;test").startsWith("<content selector=\"product-info\">"));
  }

  @Test
  public void testGetCategoryByPattern(){
    assertEquals("name",domHelper.getCategoryByPattern("your name?","your (\\w++)?"));
    assertEquals("",domHelper.getCategoryByPattern("your name?","your ([abc]++)?"));
  }


}