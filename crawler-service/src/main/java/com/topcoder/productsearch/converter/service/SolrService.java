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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
      String[] searchFields = {"manufacturer_id", "html_title", "content", "category"};
      List<String> searchQueries = new LinkedList<>();
      for (int i = 0; i < searchFields.length; i++) {
        int finalI = i;
        searchQueries.add("(" + request.getQuery().stream().map(keyword -> searchFields[finalI] + ":" + "\"" + keyword + "\"")
                .collect(Collectors.joining(" AND ")) + ")");
      }
      String q = String.join(" OR ", searchQueries);
      logger.info("search q = " + q);
      query.set("q", q);
    } else {
      query.set("q", "*:*");
    }
    query.set("start", request.getStart());
    query.set("rows", request.getRows());
    query.set("fl", "id,manufacturer_name,product_url, page_updated_at,score,category,manufacturer_id,content,html_title");
    query.set("hl", "on");
    query.set("hl.fl", "content");

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
      solrProduct.setCategory(document.get("category").toString());
      solrProduct.setDigest(Common.firstNOfString(document.get("content").toString(), request.getFirstNOfContent()));
      solrProduct.setHighlighting(getHighlighting(response, solrProduct.getId(), "content"));
      solrProduct.setTitle(document.get("html_title").toString());
      solrProduct.setManufacturerId(document.get("manufacturer_id").toString());
      products.add(solrProduct);
    }
    return products;
  }

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
    document.addField("manufacturer_id", site.getId() + " ");

    document.addField("content", new DomHelper().htmlToText(page.getContent()));
    document.addField("category", page.getCategory());
    document.addField("page_updated_at", Date.from(Instant.now()));
    return document;
  }

}
