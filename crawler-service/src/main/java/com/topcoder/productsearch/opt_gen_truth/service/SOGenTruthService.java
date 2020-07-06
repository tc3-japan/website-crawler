package com.topcoder.productsearch.opt_gen_truth.service;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gargoylesoftware.htmlunit.ProxyConfig;
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
   * the web client
   */
  private WebClient webClient;

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
   * proxy port
   */
  @Value("${optimization.gen_truth.proxy.port:9050}")
  private Integer proxyPort;

  /**
   * proxy enabled or not
   */
  @Value("${optimization.gen_truth.proxy.enabled:false}")
  private Boolean proxyEnabled = false;

  /**
   * proxy type (http | socks)
   */
  @Value("${optimization.gen_truth.proxy.type:socks}")
  private String proxyType;

  /**
   * proxy host
   */
  @Value("${optimization.gen_truth.proxy.host:127.0.0.1}")
  private String proxyHost;

  /**
   * process interval (seconds)
   */
  @Value("${optimization.gen_truth.interval:60}")
  private Integer intervalSeconds;



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
   * @param site            the website
   * @param searchWords     the search words
   * @param searchWordsPath the text file path of search words
   * @param crawl           crawl pages found in Google search if true
   */
  public void genTruth(WebSite site, String searchWords, String searchWordsPath) throws Exception {
    boolean crawl = true;

    if (StringUtils.isEmpty(searchWords) &&  StringUtils.isEmpty(searchWordsPath)) {
      logger.info("search-words or search-words-path are required");
      return;
    }

    if (searchWords != null) {
      genTruth(site, searchWords, crawl);
      return;
    }

    Path targetSearchWordsPath = Paths.get(searchWordsPath).toAbsolutePath();
    if (Files.isReadable(targetSearchWordsPath) == false) {
      logger.info("no such file \"" + searchWordsPath + "\" or could not readable");
      return;
    }
    List<String> searchWordsList = Files.readAllLines(targetSearchWordsPath, StandardCharsets.UTF_8);
    if (searchWordsList == null || searchWordsList.size() == 0) {
      logger.info("the file " + searchWordsPath + " is empty");
      return;
    }
    logger.info("# of Query: " + searchWordsList.size());
    int q = 0;
    for (String targetSearchWords : searchWordsList) {
      logger.info(String.format("Query#%d: %s", ++q, targetSearchWords));
      genTruth(site, targetSearchWords, crawl);
      logger.info(String.format("(%d/%d) done", q, searchWordsList.size()));
      if (q < searchWordsList.size()) {
        waitInterval();
      }
    }
  }

  List<String> splitAndQuote(String searchWords) {
    List<String> words = new LinkedList<String>();
    if (searchWords == null || searchWords.length() == 0) {
      return words;
    }
    return Arrays.asList(searchWords.split("\\s+")).stream().map(s -> "\"" + s +"\"").collect(Collectors.toList());
  }

  /**
   * set proxy configration to WebClient.
   */
  private void setProxyConfig(WebClient client) {
    if (proxyEnabled) {
      ProxyConfig proxyConfig = new ProxyConfig();
      proxyConfig.setProxyHost(proxyHost);
      proxyConfig.setProxyPort(proxyPort);
      if ("socks".equalsIgnoreCase(proxyType)) {
        proxyConfig.setSocksProxy(true);
      }
      client.getOptions().setProxyConfig(proxyConfig);
      logger.info(String.format("enabled proxy. type=%s host=%s port=%d", proxyType, proxyHost, proxyPort));
    }
  }

  /**
   * sleep for the interval time
   */
  void waitInterval() {
    if (intervalSeconds == null || intervalSeconds <= 0) {
      return;
    }
    System.out.print("Waiting " + intervalSeconds + " seconds.");
    for (int i = 0; i < intervalSeconds; i++) {
      try {
        Thread.sleep(1000);
        System.out.print(".");
      } catch (InterruptedException e) {
        logger.warn("InterruptedException occurred in sleep: " + e.getMessage());
      }
    }
    System.out.println("");
  }

  /**
   * create reference data by scraping the result of Google search with some search words for the specific web site.
   *
   * @param site        the website
   * @param searchWords the search words
   * @param crawl       crawl pages found in Google search if true
   */
  @Transactional
  public void genTruth(WebSite site, String searchWords, boolean crawl) throws Exception {

    String params = String.join(" ", splitAndQuote(searchWords)) + " " + (site.getGoogleParam() == null ? "" : site.getGoogleParam());

    URL url = new URL("https://www.google.com/search?q=" + params);

    logger.info("start request " + url.toString());

    setProxyConfig(this.webClient);

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
        Thread.sleep(500);
        if (threadPoolExecutor.isTerminated()) {
          break;
        }
      } catch (InterruptedException e) {
        logger.warn(e.getMessage(), e);
      }
    }

    logger.info("crawl progress finished, now start create/update solr index for pages ...");

    details.forEach(soTruthDetail -> {
      CPage cPage = pageRepository.findByUrl(soTruthDetail.getUrl());
      if (cPage == null) {
        logger.debug("page is not found in databse. url: " + soTruthDetail.getUrl());
        return;
      }
      try {
        logger.info("create/update for " + cPage.getUrl());
        solrService.createOrUpdate(cPage);
      } catch (Exception e) {
        logger.error("create/update for page " + cPage.getUrl() + " failed", e);
      }
    });

    logger.info("create/update solr index progress finished, now start update similarity scores for pages ...");

    ProductSearchRequest request = new ProductSearchRequest();
    request.setManufacturerIds(Collections.singletonList(webSite.getId()));
    request.setQuery(Arrays.asList(searchWords.split("\\s+")));
    request.setRows(numberOfUrl * 10);
    request.setWeights(Arrays.asList(new Float[] {1F, 1F, 1F, 1F, 1F, 1F, 1F, 1F, 1F, 1F}));
    request.setDebug(true);

    Map<String, SolrProduct> productMap = new HashMap<>();
    List<SolrProduct> products = solrService.searchProduct(request);
    products.forEach(solrProduct -> productMap.put(solrProduct.getUrl(), solrProduct));

    int hitCount = 0;
    for(Iterator<SOTruthDetail> iter = details.iterator(); iter.hasNext(); )  {
      SOTruthDetail soTruthDetail = iter.next();
      SolrProduct p = productMap.get(soTruthDetail.getUrl());
      if (p == null) {
        logger.info("no entry found in the API result for "  + soTruthDetail.getUrl());
        continue;
      }
      hitCount++;
      soTruthDetail.setScore(p.getScore());
      Map<String, Float> scores =
          this.getSimilarityScoresByExplain(p.getExplain());

      for (int i = 1; i <= 10; i++) {
        Common.setValueByName(soTruthDetail, "simArea" + i, scores.get("html_area" + i));
      }
      logger.info(toLog(soTruthDetail));
    };
    soTruthDetailRepository.save(details);
    logger.info(String.format("truth id: %d, query: %s, # of details: %d, # of similarities: %d",
        details.get(0).getTruthId(), searchWords, details.size(), hitCount));
    logger.info("all processes done.");
  }

  private String toLog(SOTruthDetail detail) {
    if (detail == null) {
      return "";
    }
    Function<Float, Float> f = n -> n != null ? n : 0f;
    return String.format("truth#%d-%d %s [%f, %f, %f, %f, %f, %f, %f, %f, %f, %f]",
        detail.getTruthId(), detail.getRank(), detail.getUrl(),
        f.apply(detail.getSimArea1()), f.apply(detail.getSimArea2()), f.apply(detail.getSimArea3()),
        f.apply(detail.getSimArea4()), f.apply(detail.getSimArea5()), f.apply(detail.getSimArea6()),
        f.apply(detail.getSimArea7()), f.apply(detail.getSimArea8()), f.apply(detail.getSimArea9()),
        f.apply(detail.getSimArea10()));
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
