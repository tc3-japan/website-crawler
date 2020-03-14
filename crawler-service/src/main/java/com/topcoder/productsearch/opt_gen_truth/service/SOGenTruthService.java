package com.topcoder.productsearch.opt_gen_truth.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.topcoder.productsearch.api.models.ProductSearchRequest;
import com.topcoder.productsearch.api.models.SolrProduct;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.SOTruth;
import com.topcoder.productsearch.common.entity.SOTruthDetail;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.repository.SOTruthDetailRepository;
import com.topcoder.productsearch.common.repository.SOTruthRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.converter.service.SolrService;
import com.topcoder.productsearch.crawler.CrawlerTask;
import com.topcoder.productsearch.crawler.CrawlerThread;
import com.topcoder.productsearch.crawler.CrawlerThreadPoolExecutor;
import lombok.Setter;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
   * slor service
   */
  @Autowired
  private SolrService solrService;

  /**
   * page repository
   */
  @Autowired
  PageRepository pageRepository;

  /**
   * number of expected url need search
   */
  @Value("${optimization.list_size:10}")
  private Integer numberOfUrl;

  /**
   * only search first number of pages
   */
  @Value("${optimization.search_max_pages:10}")
  private Integer searchMaxPages;

  /**
   * the web client
   */
  private WebClient webClient;

  String unzipRealUrl(String href) {
    // remove all params
    href = href.split("\\?")[0];
    // remove last /
    href = href.charAt(href.length() - 1) == '/' ? href.substring(0, href.length() - 1) : href;
    return href;
  }

  public SOGenTruthService() {
    this.webClient = Common.createWebClient();
    this.webClient.getOptions().setJavaScriptEnabled(true);
  }

  /**
   * create reference data by scraping the result of Google search with some search words for the specific web site.
   *
   * @param site        whe website
   * @param searchWords the search words
   * @param crawl       crawl pages found in Google search if true
   */
  @Transactional
  public void genTruth(WebSite site, String searchWords, boolean crawl) throws Exception {
    String params = searchWords + (site.getGoogleParam() == null ? "" : site.getGoogleParam());

    URL url = new URL("https://www.google.com/search?q=" + params);
    logger.info("start request " + url.toString());
    HtmlPage page = webClient.getPage(url);
    int rank = 1;
    int pageIndex = 0;

    SOTruth soTruth = new SOTruth();
    soTruth.setSearchWords(searchWords);
    soTruth.setSiteId(site.getId());
    soTruthRepository.save(soTruth);

    List<SOTruthDetail> details = new ArrayList<>();
    while (pageIndex < searchMaxPages) {

      // search in page N
      logger.info("start progress search page " + (pageIndex + 1));
      DomNodeList<DomNode> domNodes = page.querySelectorAll("#search .r > a");

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
          soTruthDetail.setTitle(node.querySelector("h3").asText());
          soTruthDetail.setScore(0.f);
          details.add(soTruthDetail);
          rank += 1;

          if (rank > numberOfUrl) {
            soTruthDetailRepository.save(details);
            logger.info("found all product pages, check next steps ...");
            doNext(details, crawl, site, searchWords);
            return;
          }
        }
      }

      // try to find next link
      List<HtmlAnchor> anchors = page.getByXPath("//a[@id='pnnext']");
      if (anchors.size() > 0) {
        page = anchors.get(0).click();
      } else {
        logger.info("did not find next button, check next steps ...");
        soTruthDetailRepository.save(details);
        doNext(details, crawl, site, searchWords);
        return;
      }
      pageIndex += 1;
    }

    /*
     * when reached max page, but details.length < number of expected details
     */
    soTruthDetailRepository.save(details);
    doNext(details, crawl, site, searchWords);
  }

  /**
   * crawl pages, converter, update similarity scores in SOTruthDetail list
   *
   * @param details the pages
   * @param crawl   is need do crawl
   * @param webSite the web site
   */
  void doNext(List<SOTruthDetail> details, boolean crawl, WebSite webSite, String searchWords) throws IOException, SolrServerException {
    if (details.isEmpty() || !crawl) {
      logger.info("no need crawl these pages or no pages found, will exit ...");
      return;
    }

    CrawlerThreadPoolExecutor threadPoolExecutor = new CrawlerThreadPoolExecutor(webSite.getParallelSize(),
        webSite.getCrawlInterval());
    threadPoolExecutor.setExecutedHandler(runnable -> {
    });

    details.forEach(soTruthDetail -> {
      CrawlerThread thread = new CrawlerThread();
      CrawlerTask task = new CrawlerTask(soTruthDetail.getUrl(), webSite, null);
      thread.setCrawlerTask(task);
      thread.setTaskInterval(webSite.getCrawlInterval());
      thread.setTimeout((webSite.getTimeoutPageDownload() * 60 * 1000));
      thread.setRetryTimes(webSite.getRetryTimes());
      thread.setMaxDepth(1); // only this one page
      thread.setCrawlerService(null);
      thread.init();
      threadPoolExecutor.schedule(thread, webSite.getCrawlInterval(), TimeUnit.MILLISECONDS);
    });
    threadPoolExecutor.shutdown();

    // wait CrawlerThreadPoolExecutor finished all task
    while (true) {
      try {
        Thread.sleep(100);
        if (threadPoolExecutor.isTerminated()) {
          break;
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    logger.info("crawl progress finished, now start create/update solr index for pages ...");

    details.forEach(soTruthDetail -> {
      CPage cPage = pageRepository.findByUrl(soTruthDetail.getUrl());
      try {
        solrService.createOrUpdate(cPage);
      } catch (Exception e) {
        logger.error("create/update for page " + cPage.getUrl() + " failed");
        logger.trace(e.getMessage());
        e.printStackTrace();
      }
    });

    logger.info("create/update solr index progress finished, now start update similarity scores for pages ...");

    ProductSearchRequest request = new ProductSearchRequest();
    request.setManufacturerIds(Collections.singletonList(webSite.getId()));
    request.setQuery(Arrays.asList(searchWords.split("\\s+")));
    request.setDebug(true);

    Map<String, String> explainMap = new HashMap<>();
    List<SolrProduct> products = solrService.searchProduct(request);
    products.forEach(solrProduct -> explainMap.put(solrProduct.getUrl()
        , solrProduct.getExplain()));
    details.forEach(soTruthDetail -> {
      Map<String, Float> scores =
          this.getSimilarityScoresByExplain(explainMap.get(soTruthDetail.getUrl()));

      for (int i = 1; i <= 10; i++) {
        Common.setValueByName(soTruthDetail, "simArea" + i, scores.get("html_area" + i));
      }
    });
    soTruthDetailRepository.save(details);
    logger.info("all process done, exit ...");
  }

  /**
   * get Similarity scores by explain
   *
   * @param explain the debug information
   * @return the map
   */
  Map<String, Float> getSimilarityScoresByExplain(String explain) {
    Map<String,Float> scores = new HashMap<>();

    if (explain == null) {
      return scores;
    }

    Pattern pattern = Pattern.compile("\n(.+?) (= weight.+?) ");
    Matcher matcher = pattern.matcher(explain);


    while (matcher.find()) {
      String[] parts = matcher.group(0).split("=");
      Float score = Float.parseFloat(parts[0].trim());
      String key = parts[1].split(":")[0].substring(8);
      scores.put(key, scores.getOrDefault(key, 0.0f) + score);
    }
    return scores;
  }
}
