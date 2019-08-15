package com.topcoder.productsearch.api.controller;

import com.topcoder.productsearch.api.models.ProductSearchRequest;
import com.topcoder.productsearch.converter.service.SolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class ProductControllerTest {


  @Mock
  SolrService solrService;

  @InjectMocks
  ProductController productController;

  @Test
  public void testProduct() throws IOException, SolrServerException {
    // data
    ProductSearchRequest request = new ProductSearchRequest();
    when(solrService.searchProduct(request)).thenReturn(new LinkedList<>());

    // test
    List result = productController.searchProduct(request);

    // check
    assertNotNull(result);
    assertEquals(0, result.size());
    verify(solrService).searchProduct(request);
  }
}