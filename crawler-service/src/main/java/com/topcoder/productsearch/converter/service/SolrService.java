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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

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
    SolrQuery query = new SolrQuery();
    query.set("q", "product_url:\"" + url + "\"");
    QueryResponse response = httpSolrClient.query(query);
    if (response.getResults().getNumFound() <= 0) {
      return null;
    }
    return response.getResults().get(0).get("id").toString();
  }

  /**
   * delete solr entity by url if exist
   *
   * @param url the url
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  public void deleteByURL(String url) throws IOException, SolrServerException {
    String id = findByURL(url);
    if (id != null) {
      httpSolrClient.deleteById(id);
      httpSolrClient.commit();
    }
  }

  /**
   * create or update solr entity
   *
   * @param page the page enitty
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  public void createOrUpdate(CPage page) throws IOException, SolrServerException {
    SolrInputDocument document = pageToDocument(page);
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
    SolrQuery query = new SolrQuery();

    logger.info("search product with params " + request.toString());
    if (request.getQuery() != null && request.getQuery().size() > 0) {
      List<String> searchFields = new ArrayList<>(Arrays.asList("manufacturer_id", "html_title", "content", "category"));
      for (int i = 0; i < NUMBER_OF_HTML_AREA; i++) {
        searchFields.add("html_area" + (i + 1));
      }
      List<String> searchQueries = new LinkedList<>();
      for (int i = 0; i < searchFields.size(); i++) {
        int finalI = i;
        searchQueries.add("(" + request.getQuery().stream().map(keyword -> searchFields.get(finalI) + ":" + "\"" + keyword + "\"")
                .collect(Collectors.joining(" AND ")) + ")");
      }
      String q = String.join(" OR ", searchQueries);
      query.set("q", q);
    } else {
      query.set("q", "*:*");
    }

    String qf = this.getQF(request.getManufacturerIds().isEmpty() ?
        null : webSiteRepository.findOne(request.getManufacturerIds().get(0)), request.getWeights());
    if (!qf.isEmpty()) {
      query.set("qf", qf);
      query.set("defType", "dismax");
    }

    if (!request.getManufacturerIds().isEmpty()) {
      String fq = request.getManufacturerIds().stream().map(Object::toString).collect(Collectors.joining(" "));
      query.set("fq", "manufacturer_id:(" + fq + ")");
    }
    query.set("start", request.getStart());
    query.set("rows", request.getRows());
    query.set("fl", "id,manufacturer_name,product_url, page_updated_at,score,category,manufacturer_id,content,html_title");
    query.set("hl", "on");
    query.set("hl.fl", "content");
    logger.info(query.toLocalParamsString());

    QueryResponse response = httpSolrClient.query(query);
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
      if (document.get("content") != null ) {
        solrProduct.setDigest(Common.firstNOfString(document.get("content").toString(), request.getFirstNOfContent()));
        solrProduct.setHighlighting(getHighlighting(response, solrProduct.getId(), "content"));
      }
      solrProduct.setTitle(document.get("html_title").toString());
      if (document.get("manufacturer_id") != null ) {
        solrProduct.setManufacturerId(document.get("manufacturer_id").toString());
      }
      products.add(solrProduct);
    }
    return products;
  }

  /**
   * get search qf
   * @param site the site
   * @param weights the weights
   * @return the qf string
   */
  String getQF(WebSite site, List<Float> weights) {
    List<String> qfParts = new LinkedList<>();
    for (int i = 0; i < NUMBER_OF_HTML_AREA; i++) {
      Float w = null;
      if (weights != null && weights.size() > i) {
        w = weights.get(i);
      } else if (site != null) {
        w = Common.getValueByName(site, "weight" + (i + 1));
      }
      if (w != null) {
        qfParts.add(String.format("html_area%d^%.2f", (i + 1), w));
      } else {
        qfParts.add(String.format("html_area%d", (i + 1)));
      }
    }
    return String.join(" ", qfParts);
  }
  /**
   * get Highlighting content
   * @param response the slor response
   * @param id the document id
   * @param field the document filed
   * @return the Highlighting string
   */
  private List<String> getHighlighting(QueryResponse response, String id, String field){
    if(response.getHighlighting().containsKey(id)){
      return response.getHighlighting().get(id).get(field);
    }
    return null;
  }


  /**
   * convert page entity to solr input document
   *
   * @param page the page entity
   * @return the solr input document
   * @throws IOException         if network exception happened
   * @throws SolrServerException if solr server exception happened
   */
  private SolrInputDocument pageToDocument(CPage page) throws IOException, SolrServerException {
    WebSite site = webSiteRepository.findOne(page.getSiteId());
    String id = findByURL(page.getUrl());
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
