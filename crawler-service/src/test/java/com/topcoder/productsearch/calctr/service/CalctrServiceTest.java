package com.topcoder.productsearch.calctr.service;

import com.topcoder.productsearch.calctr.model.ClickLogCount1;
import com.topcoder.productsearch.converter.service.SolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Calctr service test
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CalctrServiceTest {

  @Mock
  EntityManager entityManager;

  @Mock
  SolrService solrService;

  @Mock
  Query c1Query;

  @Mock
  Query c2Query;

  @InjectMocks
  CalctrService calctrService;

  @Before
  public void init() throws IOException, SolrServerException {
    String c1Sql = "select normalized_search_words, count(*) as cnt from click_logs where created_at > (CURRENT_DATE - 10) group by normalized_search_words";
    String c2Sql = "select normalized_search_words,page_url, count(*) as c2, max(created_date) as latest_click_date from click_logs where created_at > (CURRENT_DATE - 10) group by normalized_search_words, page_url";

    List<Object[]> count1s = new ArrayList<>();
    count1s.add(new Object[]{"sofa", 2});
    count1s.add(new Object[]{"bed", 3});

    List<Object[]> count2s = new ArrayList<>();
    count2s.add(new Object[]{"sofa", "http://sofa", 2, "2020-10-10"});
    count2s.add(new Object[]{"bed", "http://bed", 3, "2020-10-10"});
    count2s.add(new Object[]{"bed 02", "http://bed02", 3, "2020-10-10"});
    count2s.add(new Object[]{"bed 03", "http://bed03", 3, "2020-10-10"});

    when(c1Query.getResultList()).thenReturn(count1s);
    when(c2Query.getResultList()).thenReturn(count2s);
    when(entityManager.createNativeQuery(c1Sql)).thenReturn(c1Query);
    when(entityManager.createNativeQuery(c2Sql)).thenReturn(c2Query);

    List<SolrDocument> documents = new ArrayList<>();
    List<SolrDocument> excludeDocuments = new ArrayList<>();
    SolrDocument d1 = new SolrDocument();
    d1.setField("product_url", "http://sofa");
    d1.setField("ctr_term", "sofa");
    documents.add(d1);

    SolrDocument d2 = new SolrDocument();
    d2.setField("product_url", "http://bed");
    documents.add(d2);

    SolrDocument d3 = new SolrDocument();
    d3.setField("product_url", "http://bed02");
    d3.setField("ctr_term", "sofa");
    d3.setField("ctr", "1.2");
    documents.add(d3);
    excludeDocuments.add(d3);
    when(solrService.findByURLs(any())).thenReturn(documents);
    when(solrService.createSolrInputDocument(any())).thenReturn(new SolrInputDocument());
    when(solrService.findByCtrAndIds(any())).thenReturn(excludeDocuments);
  }

  /**
   * test calctr
   */
  @Test
  public void testCalctr() throws IOException, SolrServerException {
    calctrService.setP(0.5f);
    calctrService.setT(0.5f);
    calctrService.calctr(10);
    verify(solrService, times(1)).findByCtrAndIds(any());
    verify(solrService, times(1)).findByURLs(any());
    verify(solrService, times(3)).createOrUpdate(any(SolrInputDocument.class));
    verify(solrService, times(1)).createOrUpdate(any(SolrDocument.class));
  }
}
