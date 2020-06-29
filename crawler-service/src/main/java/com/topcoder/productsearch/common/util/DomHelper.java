package com.topcoder.productsearch.common.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * html dom helper
 */
public class DomHelper {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(DomHelper.class);

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

    WebClient webClient = page.getWebClient();
    //webClient.getOptions().setJavaScriptEnabled(true);
    webClient.waitForBackgroundJavaScript(10000);

    //HtmlPage refreshPage = page;
    /*
    try {
      page = (HtmlPage)page.refresh();
    } catch (IOException e) {
      logger.warn(e.getMessage());
    }
    */

    /*
     * here is a sample css selector and xpath.
     *
     * html > head > title
     * //meta[@name="description"]/@content;//meta[@name="keywords"]/@content
     * .range-breadcrumb__list
     */
    String[] selectors = cssSelectors.split("\n");
    List<String> contents = new LinkedList<>();
    for (String selector : selectors) {
      if (selector.equals("") || selector == null) {
        // next selector
        continue;
      }
      StringBuilder sb = new StringBuilder();
      // if xpath in selector
      if (selector.startsWith("/")) {
        String[] xpaths = selector.split(";");
        for (String xpath : xpaths) {
          List<DomNode> domNodes = page.getByXPath(xpath);
          for (int i = 0; i < domNodes.size(); i++) {
            sb.append(domNodes.get(i).getNodeValue() + " ");
          }
        }
      // if css selector
      } else {
        // you can check more details in here https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelector
        DomNodeList<DomNode> domNodes = page.querySelectorAll(selector);
        for (int i = 0; i < domNodes.size(); i++) {
          //sb.append(domNodes.get(i).asXml());
          sb.append(domNodes.get(i).asText() + " ");
        }
      }
      logger.debug(String.format("area#%d, content-size:%d, selector:%s", contents.size() + 1, sb.length(), selector));
      contents.add(String.format("<content selector=\"%s\">%s</content>", selector, sb.toString()));
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
    if (bodyString == null || pattern == null) {
      return null;
    }
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

  /**
   * get html area list from contents
   * @param contents the html contents
   * @return the list
   */
  public List<String> getHtmlAreasFromContents(String contents) {
    Document document = Jsoup.parse(contents);
    Elements elements = document.select("content");
    return elements.stream().map(Element::html).collect(Collectors.toList());
  }
}
