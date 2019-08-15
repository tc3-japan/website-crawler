package com.topcoder.productsearch.api.controller;

import com.topcoder.productsearch.api.models.ProductSearchRequest;
import com.topcoder.productsearch.api.models.SolrProduct;
import com.topcoder.productsearch.converter.service.SolrService;
import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * Product Controller class
 */
@RestController
@RequestMapping("/search_products")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ProductController {


  /**
   * solr service instance
   */
  @Autowired
  private SolrService solrService;

  /**
   * search product
   *
   * @param request the search request
   * @return the result
   */
  @PostMapping("/")
  public List<SolrProduct> searchProduct(@Valid @RequestBody ProductSearchRequest request)
      throws IOException, SolrServerException {
    return solrService.searchProduct(request);
  }
}