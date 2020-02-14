package com.topcoder.productsearch.opt_gen_truth.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.topcoder.productsearch.common.entity.SOTruth;
import com.topcoder.productsearch.common.entity.SOTruthDetail;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.SOTruthDetailRepository;
import com.topcoder.productsearch.common.repository.SOTruthRepository;
import com.topcoder.productsearch.common.util.Common;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.List;

/**
 * search opt gen truth service
 */
@Service
@Setter
public class SOGenTruthService {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(SOGenTruthService.class);


  /**
   * search opt truth repository
   */
  @Autowired
  private SOTruthRepository soTruthRepository;

  /**
   * search opt truth details repository
   */
  @Autowired
  private SOTruthDetailRepository soTruthDetailRepository;

  /**
   * number of expected url need search
   */
  @Value("${search-opt.number-of-url}")
  private Integer numberOfUrl;

  /**
   * only search first number of pages
   */
  @Value("${search-opt.search-max-pages}")
  private Integer searchMaxPages;

  /**
   * the web client
   */
  private WebClient webClient;

  String unzipRealUrl(String href) {
    String PREFIX = "/url?q=";
    if (!href.startsWith(PREFIX)) {
      return null;
    }
    // remove prefix
    href = href.substring(PREFIX.length());
    // remove all params
    href = href.split("&")[0];
    // remove last /
    href = href.charAt(href.length() - 1) == '/' ? href.substring(0, href.length() - 1) : href;
    return href;
  }

  public SOGenTruthService() {
    this.webClient = Common.createWebClient();
  }

  /**
   * create reference data by scraping the result of Google search with some search words for the specific web site.
   *
   * @param site        whe website
   * @param searchWords the search words
   */
  @Transactional
  public void genTruth(WebSite site, String searchWords) throws Exception {
    String params = searchWords + (site.getGoogleParam() == null ? "" : ("+" + site.getGoogleParam()));

    URL url = new URL("https://www.google.com/search?q=" + params);
    logger.info("start request " + url.toString());
    HtmlPage page = webClient.getPage(url);
    int rank = 1;
    int pageIndex = 0;

    SOTruth soTruth = new SOTruth();
    soTruth.setSearchWords(searchWords);
    soTruth.setSiteId(site.getId());
    soTruthRepository.save(soTruth);


    while (pageIndex < searchMaxPages) {

      // search in page N
      logger.info("start progress search page " + (pageIndex + 1));
      DomNodeList<DomNode> domNodes = page.querySelectorAll("a");

      for (int i = 0; i < domNodes.getLength(); i++) {
        DomNode node = domNodes.get(i);
        String href = this.unzipRealUrl(node.getAttributes().getNamedItem("href").getNodeValue());
        if (href == null) {
          continue;
        }
        if (Common.isMatch(site, href)) {
          logger.info("found expected url : " + href + ", with rank : " + rank);
          SOTruthDetail soTruthDetail = new SOTruthDetail();
          soTruthDetail.setRank(rank);
          soTruthDetail.setTruthId(soTruth.getId());
          soTruthDetail.setUrl(href);
          soTruthDetail.setTitle(node.querySelector("div").asText());
          soTruthDetail.setScore(0.f);
          soTruthDetailRepository.save(soTruthDetail);
          rank += 1;

          if (rank > numberOfUrl) {
            logger.info("found all product pages, exit ...");
            return;
          }
        }
      }

      // try to find next link
      List<HtmlAnchor> anchors = page.getByXPath("//a[@aria-label='Next page']");
      if (anchors.size() > 0) {
        page = anchors.get(0).click();
      } else {
        logger.info("did not find next button, exit ...");
        return;
      }
      pageIndex += 1;
    }
  }
}
