package com.topcoder.productsearch.common.util;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * html dom helper
 */
public class DomHelper {

  /**
   * find all urls in page
   *
   * @param page the html page
   * @return the link list
   */
  public List<String> findAllUrls(HtmlPage page) {
    DomNodeList<DomNode> domNodes = page.querySelectorAll("a");
    List<String> urls = new LinkedList<>();
    for (int i = 0; i < domNodes.size(); i++) {
      DomNode domNode = domNodes.get(i);
      Node node = domNode.getAttributes().getNamedItem("href");
      if (node == null) {
        continue;
      }
      urls.add(node.getNodeValue());
    }
    return urls;
  }
}
