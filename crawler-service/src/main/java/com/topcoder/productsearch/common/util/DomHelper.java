package com.topcoder.productsearch.common.util;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /**
   * get content list by css selector
   *
   * @param cssSelectors the css selectors, separated by commas
   * @return the list of content
   */
  public String getContentsByCssSelectors(HtmlPage page, String cssSelectors) {
    if (cssSelectors == null || cssSelectors.trim().length() == 0) {
      return page.getBody().asXml();
    }
    String[] selectors = cssSelectors.split(",");
    List<String> contents = new LinkedList<>();
    for (String selector : selectors) {
      // here you can found more example about xpath query https://howtodoinjava.com/xml/java-xpath-tutorial-example/
      // for example : "//div[contains(@class,'product-info')]" , this mean found all div and class is contains "product-info"
      // use @class="selector value" is more accurate
      List<DomNode> domNodes = page.getByXPath("//*[@class='" + selector + "']");
      domNodes.forEach(domNode -> contents.add(String.format("<content selector=\"%s\">%s</content>",
              selector, domNode.asXml())));
    }
    return String.join("\n", contents);
  }

  /**
   * get category by regex pattern
   *
   * @param bodyString    the html body
   * @param pattern the pattern
   * @return the category
   */
  public String getCategoryByPattern(String bodyString, String pattern) {
    Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
    Matcher matcher = p.matcher(bodyString);
    String category = "";
    while (matcher.find()) {
      String r = matcher.group(1);
      if (r != null) {
        category = r.trim();
      }
    }
    return category;
  }

  /**
   * get text from html
   *
   * @param htmlStr the html str
   * @return the plain text
   */
  public String htmlToText(String htmlStr) {
    return Jsoup.parse(htmlStr).text();
  }
}
