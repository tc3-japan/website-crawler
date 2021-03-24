package com.topcoder.productsearch.converter.service;

import com.topcoder.productsearch.api.models.ProductSearchRequest;
import com.topcoder.productsearch.api.models.SolrProduct;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.common.util.DomHelper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * the solr service
 */
@Service
@Setter
@Getter
public class SolrService {


  /**
   * the logger
   */
  private static final Logger logger = LoggerFactory.getLogger(SolrService.class);

  /**
   * number of html area
   */
  private static final int NUMBER_OF_HTML_AREA = 10;

  /**
   * the website repository
   */
  @Autowired
  WebSiteRepository webSiteRepository;

  /**
   * the http solr client
   */
  private HttpSolrClient httpSolrClient;

  /**
   * the retry count for errors in Solr.
   */
  @Value("${solr.retry_count:2}")
  private Integer retryCount = 2;

  /**
   * the interval seconds in retrying query to Solr.
   */
  @Value("${solr.retry_interval:3}")
  private Integer retryIntervalSeconds = 3;

  /**
   * the CTR number for search product in Solr.
   */
  @Value("${solr.ctr_number:1000}")
  private Integer ctrNumber = 1000;

  /**
   * create new solr service
   *
   * @param serverURI the solr server uri
   */
  public SolrService(@Value("${solr.uri}") String serverURI) {
    httpSolrClient = new HttpSolrClient.Builder(serverURI).build();
    httpSolrClient.setParser(new XMLResponseParser());
  }


  /**
   * find product in solr by url
   *
   * @param url the url
   * @return the solr id
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  public String findByURL(String url) throws IOException, SolrServerException {
    List<String> ids = findIdsByURL(url);
    if (ids == null || ids.isEmpty()) {
      return null;
    }
    return ids.get(0);
  }

  /**
   * find products in solr by url
   *
   * @param url the url
   * @return the solr id
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  public List<String> findIdsByURL(String url) throws IOException, SolrServerException {
    List<String> ids = new ArrayList<>();
    SolrQuery query = new SolrQuery();
    query.set("q", "product_url:\"" + url + "\"");
    QueryResponse response = httpSolrClient.query(query);
    if (response.getResults().getNumFound() <= 0) {
      return ids;
    }
    for (SolrDocument result : response.getResults()) {
      ids.add(result.get("id").toString());
    }
    return ids;
  }

  /**
   * find by urls
   *
   * @param urls the url list
   * @return the documents
   */
  public List<SolrDocument> findByURLs(List<String> urls) throws IOException, SolrServerException {
    if (urls == null || urls.size() == 0) {
      return new ArrayList<SolrDocument>(0);
    }
    SolrQuery query = new SolrQuery();
    query.set("q", "product_url:(" + StringUtils.join(urls.stream().map(u -> String.format("\"%s\"", u)).collect(Collectors.toList()), " ") + ")");
    logger.info("findByUrls where q = " + query.get("q"));
    return httpSolrClient.query(query).getResults();
  }

  /**
   * find all ctr document and not in id array
   *
   * @param excludes the exclude
   * @return the documents
   */
  public List<SolrDocument> findDocsHavingCTR(List<String> excludes) throws IOException, SolrServerException {
    String qTpl = "ctr:[* TO *]";
    if (excludes != null && excludes.size() > 0) {
      qTpl += " AND %s";
    }
    SolrQuery query = new SolrQuery();
    query.set("q", String.format(qTpl, StringUtils.join(excludes.stream().map(e -> String.format("-id:\"%s\"", e)).collect(Collectors.toList()), " AND ")));
    logger.info("findByCtrAndIds where q = " + query.get("q"));
    return httpSolrClient.query(query).getResults();
  }

  /**
   * find product in solr by document ID
   *
   * @param id
   * @return the solr id
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  public SolrDocument findDocumentById(String id) throws IOException, SolrServerException {
    SolrQuery query = new SolrQuery();
    query.set("q", String.format("id:\"%s\"", id));
    query.set("fl", "id,product_url,manufacturer_id,manufacturer_name,page_updated_at,category,content,html_title,html_area1,html_area2,html_area3,html_area4,html_area5,html_area6,html_area7,html_area8,html_area9,html_area10,html_body,search_word_txt_ja,ctr_f,search_word2_txt_ja,ctr2_f");

    QueryResponse response = httpSolrClient.query(query);
    if (response.getResults().getNumFound() <= 0) {
      return null;
    }
    return response.getResults().get(0);
  }

  /**
   * delete solr entity by url if exist
   *
   * @param url the url
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  public void deleteByURL(String url) throws IOException, SolrServerException {
    List<String> ids = findIdsByURL(url);
    for (String id : ids) {
      delete(id);
    }
  }

  /**
   * delete solr entity by document ID if exist
   *
   * @param id the document ID
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  public void delete(String id) throws IOException, SolrServerException {
    if (id == null) {
      return;
    }
    httpSolrClient.deleteById(id);
    httpSolrClient.commit();
  }

  /**
   * create or update solr entity
   *
   * @param page the page enitty
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  public void createOrUpdate(CPage page) throws IOException, SolrServerException {
    List<SolrInputDocument> documents = pageToDocuments(page);
    for (SolrInputDocument document : documents) {
      createOrUpdate(document);
    }
  }

  public SolrInputDocument createSolrInputDocument(SolrDocument document) throws IOException, SolrServerException {
    if (document == null) {
      throw new IllegalArgumentException("document is required.");
    }

    SolrInputDocument input = new SolrInputDocument();
    for (String field : document.getFieldNames()) {
      input.addField(field, document.getFirstValue(field));
    }
    if (document.hasChildDocuments()) {
      for (SolrDocument childDoc : document.getChildDocuments()) {
        input.addChildDocument(createSolrInputDocument(childDoc));
      }
    }
    return input;
  }

  public void createOrUpdate(SolrDocument document) throws IOException, SolrServerException {
    SolrInputDocument input = createSolrInputDocument(document);
    createOrUpdate(input);
  }

  public void createOrUpdate(SolrInputDocument document) throws IOException, SolrServerException {
    httpSolrClient.add(document);
    httpSolrClient.commit();
  }


  /**
   * search product
   *
   * @param request the search request
   * @return the list of product
   */
  public List<SolrProduct> searchProduct(ProductSearchRequest request) throws IOException, SolrServerException {
    if (request == null) {
      throw new IllegalArgumentException("request must be specified.");
    }

    logger.info("search product with params: " + request.toString());

    boolean dismax = "dismax".equalsIgnoreCase(request.getParser());

    SolrQuery query = dismax ? buildDismaxQueryBase(request) // dismax query parser
        : buildStandardQueryBase(request); // standard query parser

    String fq = null;
    if (request.getManufacturerIds() != null && !request.getManufacturerIds().isEmpty()) {
      fq = request.getManufacturerIds().stream().map(Object::toString).collect(Collectors.joining(" "));
    }
    if (fq != null) {
      query.setFilterQueries("manufacturer_id:(" + fq + ")", "{!collapse field=product_url}");
    } else {
      query.setFilterQueries("{!collapse field=product_url}");
    }
    //query.setFilterQueries("{!collapse field=product_url}");
    //query.set("expand", true);
    query.set("start", request.getStart());
    query.set("rows", request.getRows());
    query.set("fl", "id,score,product_url,html_title,manufacturer_id,manufacturer_name,page_updated_at,category,content");
    query.set("hl", "on");
    query.set("hl.fl", "content");
    query.setShowDebugInfo(request.isDebug());

    logger.info(query.toLocalParamsString());

    QueryResponse response = execute(query);

    Map<String, String> debugInfo = new HashMap<>();
    if (request.isDebug() && response.getDebugMap() != null) {
      response.getDebugMap().forEach((key, obj) -> {
        if (key.equalsIgnoreCase("explain")) {
          SimpleOrderedMap explain = (SimpleOrderedMap) obj;
          for (int i = 0; i < explain.size(); i++) {
            debugInfo.put(explain.getName(i), explain.getVal(i).toString());
          }
        }
      });
    }
    //response.getExpandedResults();
    List<SolrProduct> products = new LinkedList<>();
    for (int i = 0; i < response.getResults().size(); i++) {
      SolrProduct solrProduct = new SolrProduct();
      SolrDocument document = response.getResults().get(i);
      solrProduct.setId(document.get("id").toString());
      solrProduct.setLastModifiedAt((Date) document.getFieldValue("page_updated_at"));
      solrProduct.setManufacturerName(document.get("manufacturer_name").toString());
      solrProduct.setScore(Float.valueOf(document.get("score").toString()));
      solrProduct.setUrl(document.get("product_url").toString());
      if (document.get("category") != null) {
        solrProduct.setCategory(document.get("category").toString());
      }
      if (document.get("content") != null) {
        solrProduct.setDigest(Common.firstNOfString(document.get("content").toString(), request.getFirstNOfContent()));
        solrProduct.setHighlighting(getHighlighting(response, solrProduct.getId(), "content"));
      }
      solrProduct.setTitle(document.get("html_title").toString());
      if (document.get("manufacturer_id") != null) {
        solrProduct.setManufacturerId(document.get("manufacturer_id").toString());
      }
      solrProduct.setExplain(debugInfo.get(solrProduct.getId()));
      products.add(solrProduct);
    }
    return products;
  }


  /**
   * Execute a query to Solr. The query will retry for specified times when some error occurred in Solr.
   *
   * @param query
   * @return
   * @throws SolrServerException
   * @throws IOException
   */
  protected QueryResponse execute(SolrQuery query) throws SolrServerException, IOException {
    for (int i = 0; i <= retryCount; i++) {
      try {
        logger.debug(String.format("[%d-Started ] SolrClient.query(%s)", Thread.currentThread().getId(), query.get("q")));
        return httpSolrClient.query(query);
      } catch (SolrException | SolrServerException | IOException e) {
        logger.error("Error occurred in processing query. " + e.getMessage());
        if (i >= retryCount) {
          throw e;
        }
        logger.info(String.format("Retry query[%d] after %d second(s).", (i + 1), retryIntervalSeconds));
        try {
          Thread.sleep(retryIntervalSeconds * 1000L);
        } catch (InterruptedException ie) {
          logger.warn(ie.getMessage());
        }
      } catch (RuntimeException e) {
        logger.error("Error occrurred in processing Solr request: " + query, e);
        throw e;
      } finally {
        logger.debug(String.format("[%d-Finished] SolrClient.query(%s)", Thread.currentThread().getId(), query.get("q")));
      }
    }
    throw new IOException("Failed to query."); // never reach here.
  }

  /**
   * build SolrQuery to use Standard Query Parser
   *
   * @param request
   * @return
   */
  SolrQuery buildStandardQueryBase(ProductSearchRequest request) {

    SolrQuery query = new SolrQuery();

    if (request.getQuery() == null || request.getQuery().isEmpty()) {
      query.set("q", "*:*");
      return query;
    }

    String q = String.format("%s", String.join(" ", request.getQuery()));

    // Normalize the query string
    q = Common.normalizeSearchWord(q);

    List<Float> weights = request.getWeights();
    if (weights == null || weights.isEmpty()) {
      if (request.getManufacturerIds() != null && !request.getManufacturerIds().isEmpty()) {
        WebSite site = webSiteRepository.findOne(request.getManufacturerIds().get(0));
        if (site != null) {
          weights = site.getWeights();
        }
      }
    }
    List<String> fieldQueries = new ArrayList<String>(NUMBER_OF_HTML_AREA);
    for (int i = 0; i < NUMBER_OF_HTML_AREA; i++) {
      Float w = null;
      if (weights != null && i < weights.size()) {
        w = weights.get(i);
      }
      String qi = String.format("html_area%d:%s^%f", (i + 1), "(" + q + ")", (w != null ? w : 1f));
      fieldQueries.add(qi);
    }

    query.set("q", "{!boost b=\"sum(1,mul(if(eq(ctr_term,\'" + q + "\'),ctr,0)," + ctrNumber + "))\"}"
        +  String.join(" OR ", fieldQueries));
    logger.debug("query: " + query.getQuery());

    return query;
  }

  /**
   * build SolrQuery to use Dismax Query Parser
   *
   * @param request
   * @return
   */
  SolrQuery buildDismaxQueryBase(ProductSearchRequest request) {

    SolrQuery query = new SolrQuery();

    query.set("defType", "dismax");
    if (request.getQuery() != null) {
      query.set("q", String.join(" ", request.getQuery()));
    } else {
      query.set("q.alt", "*:*");
    }
    String qf = this.getQF(request.getManufacturerIds().isEmpty() ?
        null : webSiteRepository.findOne(request.getManufacturerIds().get(0)), request.getWeights());
    query.set("qf", qf);

    return query;
  }

  /**
   * get search qf
   *
   * @param site    the site
   * @param weights the weights
   * @return the qf string
   */
  String getQF(WebSite site, List<Float> weights) {
    List<String> qfParts = new LinkedList<>();
    if (weights != null && weights.size() > 0) {
      int i = 0;
      for (Float w : weights) {
        qfParts.add(String.format("html_area%d^%.2f", (++i), (w != null ? w : 0f)));
      }
      return String.join(" ", qfParts);
    }
    if (site != null) {
      for (int i = 1; i <= NUMBER_OF_HTML_AREA; i++) {
        Float w = Common.getValueByName(site, "weight" + i);
        if (w != null) {
          qfParts.add(String.format("html_area%d^%.2f", i, w));
        }
      }
    }
    if (qfParts.size() > 0) {
      return String.join(" ", qfParts);
    }
    for (int i = 1; i <= NUMBER_OF_HTML_AREA; i++) {
      qfParts.add(String.format("html_area%d", i));
    }
    return String.join(" ", qfParts);
  }

  /**
   * get Highlighting content
   *
   * @param response the slor response
   * @param id       the document id
   * @param field    the document filed
   * @return the Highlighting string
   */
  private List<String> getHighlighting(QueryResponse response, String id, String field) {
    if (response.getHighlighting().containsKey(id)) {
      return response.getHighlighting().get(id).get(field);
    }
    return null;
  }

  /**
   * convert page entity to solr input documents
   *
   * @param page the page entity
   * @return the solr input documents
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  private List<SolrInputDocument> pageToDocuments(CPage page) throws IOException, SolrServerException {
    WebSite site = webSiteRepository.findOne(page.getSiteId());
    List<SolrInputDocument> documents = new ArrayList<>();
    List<String> ids = findIdsByURL(page.getUrl());
    if (!ids.isEmpty()) {
      for (String id : findIdsByURL(page.getUrl())) {
        SolrInputDocument document = pageToDocument(page, id, site);
        documents.add(document);
      }
    } else {
      SolrInputDocument document = pageToDocument(page, null, site);
      documents.add(document);
    }
    return documents;
  }

  /**
   * convert page entity to solr input document
   * 
   * @param page the page entity
   * @param id
   * @param site
   * @return the solr input document
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  private SolrInputDocument pageToDocument(CPage page, String id, WebSite site) throws IOException, SolrServerException {
    DomHelper domHelper = new DomHelper();

    // set id if exist
    SolrInputDocument document = new SolrInputDocument();
    if (id != null) {
      document.addField("id", id);
    }
    document.addField("manufacturer_name", site.getName());
    document.addField("product_url", page.getUrl());
    document.addField("html_title", page.getTitle());
    document.addField("html_body", page.getBody());

    // force convert to string for solr document
    // "1" will identify as number in solr document
    document.addField("manufacturer_id", site.getId() + "");

    document.addField("content", domHelper.htmlToText(page.getContent()));
    document.addField("category", page.getCategory());
    document.addField("page_updated_at", Date.from(Instant.now()));

    List<String> htmlAreas = domHelper.getHtmlAreasFromContents(page.getContent());
    for (int i = 0; i < htmlAreas.size(); i++) {
      document.addField("html_area" + (i + 1), htmlAreas.get(i));
    }
    return document;
  }
}
