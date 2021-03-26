package com.topcoder.productsearch.converter.service;

import com.topcoder.productsearch.AbstractUnitTest;
import com.topcoder.productsearch.api.models.ProductSearchRequest;
import com.topcoder.productsearch.api.models.SolrProduct;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * the solr service unit tests
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class SolrServiceTest extends AbstractUnitTest {

  @Mock
  HttpSolrClient httpSolrClient;

  @Mock
  QueryResponse queryResponse;

  @Spy
  SolrDocumentList solrDocumentList;

  @Mock
  SolrDocument solrDocument;

  @Mock
  WebSiteRepository webSiteRepository;

  private SolrService solrService;

  @Before
  public void init() throws IOException, SolrServerException {
    solrService = new SolrService("http://test.com");
    solrService.setHttpSolrClient(httpSolrClient);
    solrService.setWebSiteRepository(webSiteRepository);

    when(httpSolrClient.getBaseURL()).thenReturn("http://test.com");
    SolrDocument dummy = doReturn(solrDocument).when(solrDocumentList).get(0);
    System.out.println(dummy); // Dummy code to avoind spotbugs failure.
    when(solrDocument.get("id")).thenReturn("id");
    when(queryResponse.getResults()).thenReturn(solrDocumentList);

    SolrQuery query = new SolrQuery();
    query.set("q", "product_url:\"test\"");
    when(httpSolrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
    when(webSiteRepository.findOne(any(Integer.class))).thenReturn(createWebSite());
  }

  @Test
  public void testNewItem() {
    assertEquals("http://test.com", solrService.getHttpSolrClient().getBaseURL());
  }

  @Test
  public void testFindByURL() throws IOException, SolrServerException {
    when(solrDocumentList.getNumFound()).thenReturn(0L);
    assertEquals(null, solrService.findByURL("test"));

    solrDocumentList.add(solrDocument);
    when(solrDocumentList.getNumFound()).thenReturn(1L);
    assertEquals("id", solrService.findByURL("test"));
  }

  @Test
  public void testFindIdsByURL() throws IOException, SolrServerException {
    when(solrDocumentList.getNumFound()).thenReturn(0L);
    assertNotEquals(null, solrService.findIdsByURL("test"));
    assert(solrService.findIdsByURL("test").isEmpty());

    solrDocumentList.add(solrDocument);
    when(solrDocumentList.getNumFound()).thenReturn(1L);
    assertNotEquals(null, solrService.findIdsByURL("test"));
    assertEquals(1, solrService.findIdsByURL("test").size());
    assertEquals("id", solrService.findIdsByURL("test").get(0));
  }

  @Test
  public void testDelete() throws IOException, SolrServerException {
    when(solrDocumentList.getNumFound()).thenReturn(0L);
    solrService.deleteByURL("test");
    verify(httpSolrClient, times(0)).deleteById(any(String.class));

    solrDocumentList.add(solrDocument);
    when(solrDocumentList.getNumFound()).thenReturn(1L);
    solrService.deleteByURL("test");
    verify(httpSolrClient, times(1)).deleteById(any(String.class));
    verify(httpSolrClient, times(1)).commit();
  }

  @Test
  public void testCreateOrUpdate() throws IOException, SolrServerException {
    CPage cPage = new CPage();
    cPage.setUrl("http://test.com");
    cPage.setContent("<content>test</content>");
    when(solrDocumentList.getNumFound()).thenReturn(0L);
    solrService.createOrUpdate(cPage);
    verify(httpSolrClient, times(1)).add(any(SolrInputDocument.class));
    verify(httpSolrClient, times(1)).commit();

    when(solrDocumentList.getNumFound()).thenReturn(1L);
    solrService.createOrUpdate(cPage);
    verify(httpSolrClient, times(2)).add(any(SolrInputDocument.class));
    verify(httpSolrClient, times(2)).commit();
  }

  @Test
  public void testSearchProduct() throws IOException, SolrServerException {

    ProductSearchRequest request = new ProductSearchRequest();
    List<String> query = new LinkedList<>();
    query.add("women");
    query.add("leg");
    request.setQuery(query);
    List<Float> weights = new ArrayList<>();
    weights.add(1.0f);
    weights.add(2.0f);
    request.setManufacturerIds(Collections.singletonList(1));
    request.setWeights(weights);

    when(solrDocumentList.size()).thenReturn(10);
    when(solrDocumentList.get(any(Integer.class))).thenReturn(solrDocument);
    when(solrDocument.get("id")).thenReturn("id");
    when(solrDocument.getFieldValue("page_updated_at")).thenReturn(Date.from(Instant.now()));
    when(solrDocument.get("manufacturer_name")).thenReturn("manufacturer_name");
    when(solrDocument.get("score")).thenReturn("1.23");
    when(solrDocument.get("product_url")).thenReturn("product_url");
    when(solrDocument.get("category")).thenReturn("category");
    when(solrDocument.get("content")).thenReturn("content");
    when(solrDocument.get("html_title")).thenReturn("html_title");
    when(solrDocument.get("manufacturer_id")).thenReturn("manufacturer_id");

    List<SolrProduct> solrProducts = solrService.searchProduct(request);

    verify(httpSolrClient, times(1)).query(any(SolrQuery.class));
    assertEquals(10, solrProducts.size());

    Map<String, Map<String,List<String>>> highlighting = new HashMap<>();
    Map<String,List<String>> idH = new HashMap<>();
    highlighting.put("id",idH);
    idH.put("content",new LinkedList<>());
    idH.get("content").add("<em>test</em>");
    when(queryResponse.getHighlighting()).thenReturn(highlighting);
    solrService.searchProduct(request);
    verify(httpSolrClient, times(2)).query(any(SolrQuery.class));
  }


  @Test
  public void testSearchProductEmpty() throws IOException, SolrServerException {
    ProductSearchRequest request = new ProductSearchRequest();
    when(solrDocumentList.size()).thenReturn(0);
    List<SolrProduct> solrProducts = solrService.searchProduct(request);
    verify(httpSolrClient, times(1)).query(any(SolrQuery.class));
    assertEquals(0, solrProducts.size());
  }

  @Test
  public void testSearchProductEmpty02() throws IOException, SolrServerException {
    ProductSearchRequest request = new ProductSearchRequest();
    request.setQuery(new LinkedList<>());
    when(solrDocumentList.size()).thenReturn(0);
    List<SolrProduct> solrProducts = solrService.searchProduct(request);
    verify(httpSolrClient, times(1)).query(any(SolrQuery.class));
    assertEquals(0, solrProducts.size());
  }

  @Test
  public void testHGetQF() {
    WebSite site = new WebSite();
    site.setWeight2(1.0f);

    List<Float> weights = new ArrayList<>();
    weights.add(2.0f);

    assertEquals("html_area1^2.00", solrService.getQF(site, weights));

    assertEquals("html_area2^1.00", solrService.getQF(site, null));

    assertEquals("html_area1 html_area2 html_area3 html_area4 html_area5 html_area6 html_area7 html_area8 html_area9 html_area10",
      solrService.getQF(null, null));
  }

  @Test
  public void testFindByURLs() throws IOException, SolrServerException {
    when(solrDocumentList.size()).thenReturn(1);
    List<String> urls = new ArrayList<>();
    urls.add("http://gooogle.com");
    List<SolrDocument> documents = solrService.findByURLs(urls);
    verify(httpSolrClient, times(1)).query(any(SolrQuery.class));
    assertEquals(1, documents.size());
  }

  @Test
  public void testFindFindByURLs() throws IOException, SolrServerException {
    when(solrDocumentList.size()).thenReturn(1);
    List<String> ids = new ArrayList<>();
    ids.add(UUID.randomUUID().toString());
    List<SolrDocument> documents = solrService.findDocsHavingCTR(ids);
    verify(httpSolrClient, times(1)).query(any(SolrQuery.class));
    assertEquals(1, documents.size());
  }
}
