package com.topcoder.productsearch.converter.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.topcoder.productsearch.AbstractUnitTest;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.converter.ConverterException;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class ConverterServiceTest extends AbstractUnitTest {

  @Mock
  private WebSiteRepository webSiteRepository;

  @Mock
  private PageRepository pageRepository;

  @Mock
  private SolrClient solrClient;

  /**
   * The converter service to test.
   */
  private ConverterService converterService;

  @Before
  public void setUp() {
    converterService = new ConverterService(webSiteRepository, pageRepository, solrClient);
    converterService.setBatchSize(10);
    converterService.setTimeLapsePeriodInDays(5);
  }

  @Test(expected = ConverterException.class)
  public void testProcessNonExistingWebSite() throws ConverterException {
    when(webSiteRepository.findOne(1)).thenReturn(null);
    converterService.process(1, true);
  }

  @Test
  public void testProcessAllWebSitesFully()
      throws ConverterException, IOException, SolrServerException {
    int nWebSites = 10;
    int nDeletedPages = 3;
    int nNormalPages = 4;
    // Find web sites.
    when(webSiteRepository.findAll())
        .thenReturn(createWebSites(nWebSites, null, null));
    // Find deleted pages for one website.
    when(pageRepository.findAllWebPages(anyInt(), eq("product"), eq(true),
        any(Pageable.class)))
        .then(invocation ->
            new PageImpl<CPage>(
                createPages(nDeletedPages, invocation.getArgumentAt(0, Integer.class), true),
                invocation.getArgumentAt(3, Pageable.class), nDeletedPages));
    // Find pages to add/update for one website
    when(pageRepository.findAllWebPages(anyInt(), eq("product"), eq(false),
        any(Pageable.class)))
        .then(invocation -> new PageImpl<CPage>(
            createPages(nNormalPages, invocation.getArgumentAt(0, Integer.class), false),
            invocation.getArgumentAt(3, Pageable.class), nNormalPages));
    // Mark expired pages as deleted.
    when(pageRepository
        .markExpiredWebPagesAsDeleted(anyInt(), eq("product"), any(Date.class),
            any(Date.class))).thenReturn(0);
    // SolrClient delete
    when(solrClient.deleteById(eq("manufacturer_product"), anyListOf(String.class)))
        .thenReturn(new UpdateResponse());
    // SolrClient add
    when(solrClient.add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class)))
        .thenReturn(new UpdateResponse());

    converterService.process(null, false);

    verify(webSiteRepository, times(1)).findAll();
    verify(webSiteRepository, times(nWebSites)).save(any(WebSite.class));

    verify(pageRepository, times(nWebSites))
        .findAllWebPages(anyInt(), eq("product"), eq(true), any(Pageable.class));
    verify(pageRepository, times(nWebSites))
        .findAllWebPages(anyInt(), eq("product"), eq(false), any(Pageable.class));
    verify(pageRepository, times(nWebSites))
        .markExpiredWebPagesAsDeleted(anyInt(), eq("product"), any(Date.class),
            any(Date.class));
    verify(pageRepository, times(nWebSites * 2))
        .save(anyListOf(CPage.class));

    verify(solrClient, times(nWebSites))
        .add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class));
    verify(solrClient, times(2 * nWebSites))
        .commit(eq("manufacturer_product"));
  }

  @Test
  public void testProcessNullSiteIdCleanupOnly()
      throws ConverterException, IOException, SolrServerException {
    int nWebSites = 10;
    int nDeletedPages = 3;
    int nNormalPages = 4;
    // Find web sites.
    when(webSiteRepository.findAll())
        .thenReturn(createWebSites(nWebSites, null, null));
    // Find deleted pages for one website.
    when(pageRepository.findAllWebPages(anyInt(), eq("product"), eq(true),
        any(Pageable.class)))
        .then(invocation ->
            new PageImpl<>(
                createPages(nDeletedPages, invocation.getArgumentAt(0, Integer.class), true),
                invocation.getArgumentAt(3, Pageable.class), nDeletedPages));
    // Find pages to add/update for one website
    when(pageRepository.findAllWebPages(anyInt(), eq("product"), eq(false),
        any(Pageable.class)))
        .then(invocation -> new PageImpl<>(
            createPages(nNormalPages, invocation.getArgumentAt(0, Integer.class), false),
            invocation.getArgumentAt(3, Pageable.class), nNormalPages));
    // Mark expired pages as deleted.
    when(pageRepository
        .markExpiredWebPagesAsDeleted(anyInt(), eq("product"), any(Date.class),
            any(Date.class))).thenReturn(0);
    // SolrClient delete
    when(solrClient.deleteById(eq("manufacturer_product"), anyListOf(String.class)))
        .thenReturn(new UpdateResponse());
    // SolrClient add
    when(solrClient.add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class)))
        .thenReturn(new UpdateResponse());

    converterService.process(null, true);

    verify(webSiteRepository, times(1)).findAll();
    verify(webSiteRepository, times(nWebSites)).save(any(WebSite.class));

    verify(pageRepository, times(nWebSites))
        .findAllWebPages(anyInt(), eq("product"), eq(true), any(Pageable.class));
    // Not processing any of the pages.
    verify(pageRepository, times(0))
        .findAllWebPages(anyInt(), eq("product"), eq(false), any(Pageable.class));
    verify(pageRepository, times(nWebSites))
        .markExpiredWebPagesAsDeleted(anyInt(), eq("product"), any(Date.class),
            any(Date.class));

    verify(solrClient, times(0))
        .add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class));
    verify(solrClient, times(nWebSites))
        .deleteById(eq("manufacturer_product"), anyListOf(String.class));
    verify(solrClient, times(nWebSites))
        .commit(eq("manufacturer_product"));
  }

  @Test
  public void testProcessOneWebSiteFully()
      throws IOException, SolrServerException, ConverterException {
    WebSite webSite = createWebSite();
    int nDeletedPages = 10;
    int nNormalPages = 10;
    int nBatches = 5;
    when(webSiteRepository.findOne(eq(1)))
        .thenReturn(webSite);
    // Find deleted pages for one website.
    when(pageRepository.findAllWebPages(eq(1), eq("product"), eq(true),
        any(Pageable.class)))
        .then(invocation ->
            new PageImpl<>(
                createPages(nDeletedPages, invocation.getArgumentAt(0, Integer.class), true),
                invocation.getArgumentAt(3, Pageable.class), 50L));
    // Find pages to add/update for one website
    when(pageRepository.findAllWebPages(eq(1), eq("product"), eq(false),
        any(Pageable.class)))
        .then(invocation -> new PageImpl<>(
            createPages(nNormalPages, invocation.getArgumentAt(0, Integer.class), false),
            invocation.getArgumentAt(3, Pageable.class), 50L));
    // Mark expired pages as deleted.
    when(pageRepository
        .markExpiredWebPagesAsDeleted(eq(1), eq("product"), any(Date.class),
            any(Date.class))).thenReturn(0);
    // SolrClient delete
    when(solrClient.deleteById(eq("manufacturer_product"), anyListOf(String.class)))
        .thenReturn(new UpdateResponse());
    // SolrClient add
    when(solrClient.add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class)))
        .thenReturn(new UpdateResponse());

    converterService.process(1, false);

    Assert.assertNotNull(webSite.getLastProcessedAt());
    Assert.assertNotNull(webSite.getLastCleanedUpAt());

    verify(webSiteRepository, times(1)).findOne(eq(1));
    verify(webSiteRepository, times(1)).save(webSite);

    verify(pageRepository, times(nBatches)).findAllWebPages(eq(1), eq("product"), eq(true),
        any(Pageable.class));
    verify(pageRepository, times(nBatches))
        .findAllWebPages(eq(1), eq("product"), eq(false), any(Pageable.class));
    verify(pageRepository, times(1))
        .markExpiredWebPagesAsDeleted(eq(1), eq("product"), any(Date.class), any(Date.class));
    verify(solrClient, times(nBatches))
        .deleteById(eq("manufacturer_product"), anyListOf(String.class));
    verify(solrClient, times(nBatches))
        .add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class));
    verify(solrClient, times(2))
        .commit(eq("manufacturer_product"));
  }

  @Test
  public void testProcessOneWebSiteFullyWithLastProcessedAndLastCleanedAt()
      throws IOException, SolrServerException, ConverterException {
    WebSite webSite = createWebSite();
    webSite.setLastProcessedAt(new Date());
    webSite.setLastCleanedUpAt(new Date());
    final Date lastProcessedAt = webSite.getLastProcessedAt();
    final Date lastCleanedUpAt = webSite.getLastCleanedUpAt();
    int nDeletedPages = 10;
    int nNormalPages = 10;
    int nBatches = 5;
    when(webSiteRepository.findOne(eq(1)))
        .thenReturn(webSite);
    // Find deleted pages for one website.
    when(pageRepository
        .findModifiedWebPages(eq(1), eq("product"), eq(lastCleanedUpAt), eq(true),
            any(Pageable.class)))
        .then(invocation ->
            new PageImpl<>(
                createPages(nDeletedPages, invocation.getArgumentAt(0, Integer.class), true),
                invocation.getArgumentAt(4, Pageable.class), 50L));
    // Find pages to add/update for one website
    when(pageRepository
        .findModifiedWebPages(eq(1), eq("product"), eq(lastProcessedAt), eq(false),
            any(Pageable.class)))
        .then(invocation -> new PageImpl<>(
            createPages(nNormalPages, invocation.getArgumentAt(0, Integer.class), false),
            invocation.getArgumentAt(4, Pageable.class), 50L));
    // Mark expired pages as deleted.
    when(pageRepository
        .markExpiredWebPagesAsDeleted(eq(1), eq("product"), any(Date.class),
            any(Date.class))).thenReturn(0);
    // SolrClient delete
    when(solrClient.deleteById(eq("manufacturer_product"), anyListOf(String.class)))
        .thenReturn(new UpdateResponse());
    // SolrClient add
    when(solrClient.add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class)))
        .thenReturn(new UpdateResponse());

    converterService.process(1, false);

    verify(webSiteRepository, times(1)).findOne(eq(1));
    verify(webSiteRepository, times(1)).save(webSite);

    verify(pageRepository, times(nBatches)).findModifiedWebPages(eq(1), eq("product"),
        eq(lastCleanedUpAt), eq(true), any(Pageable.class));
    verify(pageRepository, times(nBatches)).findModifiedWebPages(eq(1), eq("product"),
        eq(lastProcessedAt), eq(false), any(Pageable.class));
    verify(pageRepository, times(1))
        .markExpiredWebPagesAsDeleted(eq(1), eq("product"), any(Date.class), any(Date.class));
    verify(solrClient, times(nBatches))
        .deleteById(eq("manufacturer_product"), anyListOf(String.class));
    verify(solrClient, times(nBatches))
        .add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class));
    verify(solrClient, times(2))
        .commit(eq("manufacturer_product"));
  }

  @Test
  public void testPageCounters1() throws IOException, SolrServerException, ConverterException {
    WebSite webSite = createWebSite();
    CPage cPage = createPage();

    webSite.setLastProcessedAt(new Date());
    webSite.setLastCleanedUpAt(new Date());

    final Date lastProcessedAt = webSite.getLastProcessedAt();
    final Date lastCleanedUpAt = webSite.getLastCleanedUpAt();

    when(webSiteRepository.findOne(eq(1)))
        .thenReturn(webSite);
    // Find deleted pages for one website.
    when(pageRepository
        .findModifiedWebPages(eq(1), eq("product"), eq(lastCleanedUpAt), eq(true),
            any(Pageable.class)))
        .then(invocation ->
            new PageImpl<>(Collections.emptyList(),
                invocation.getArgumentAt(4, Pageable.class), 0));
    // Find pages to add/update for one website
    when(pageRepository
        .findModifiedWebPages(eq(1), eq("product"), eq(lastProcessedAt), eq(false),
            any(Pageable.class)))
        .then(invocation -> new PageImpl<>(
            Collections.singletonList(cPage),
            invocation.getArgumentAt(4, Pageable.class), 1));
    // Mark expired pages as deleted.
    when(pageRepository
        .markExpiredWebPagesAsDeleted(eq(1), eq("product"), any(Date.class),
            any(Date.class))).thenReturn(13);
    // SolrClient add
    when(solrClient.add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class)))
        .thenReturn(new UpdateResponse());

    converterService.process(1, false);

    assertNotNull(cPage.getLastProcessedAt());
    assertThat(converterService.getPagesAdded(), is(1));
    assertThat(converterService.getPagesExpired(), is(13));
    assertThat(converterService.getPagesUpdated(), is(0));
    assertThat(converterService.getPagesDeleted(), is(0));

    verify(webSiteRepository, times(1)).findOne(eq(1));
    verify(webSiteRepository, times(1)).save(webSite);

    verify(pageRepository, times(1)).findModifiedWebPages(eq(1), eq("product"),
        eq(lastCleanedUpAt), eq(true), any(Pageable.class));
    verify(pageRepository, times(1)).findModifiedWebPages(eq(1), eq("product"),
        eq(lastProcessedAt), eq(false), any(Pageable.class));
    verify(pageRepository, times(1))
        .markExpiredWebPagesAsDeleted(eq(1), eq("product"), any(Date.class), any(Date.class));
    verify(pageRepository, times(1))
        .save(anyListOf(CPage.class));

    verify(solrClient, times(0))
        .deleteById(eq("manufacturer_product"), anyListOf(String.class));
    verify(solrClient, times(1))
        .add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class));
    verify(solrClient, times(2))
        .commit(eq("manufacturer_product"));
  }

  @Test
  public void testPageCounters2() throws IOException, SolrServerException, ConverterException {
    WebSite webSite = createWebSite();
    CPage cPage = createPage();
    cPage.setLastProcessedAt(new Date());

    webSite.setLastProcessedAt(new Date());
    webSite.setLastCleanedUpAt(new Date());

    final Date lastProcessedAt = webSite.getLastProcessedAt();
    final Date lastCleanedUpAt = webSite.getLastCleanedUpAt();

    when(webSiteRepository.findOne(eq(1)))
        .thenReturn(webSite);
    // Find deleted pages for one website.
    when(pageRepository
        .findModifiedWebPages(eq(1), eq("product"), eq(lastCleanedUpAt), eq(true),
            any(Pageable.class)))
        .then(invocation ->
            new PageImpl<>(Collections.emptyList(),
                invocation.getArgumentAt(4, Pageable.class), 0));
    // Find pages to add/update for one website
    when(pageRepository
        .findModifiedWebPages(eq(1), eq("product"), eq(lastProcessedAt), eq(false),
            any(Pageable.class)))
        .then(invocation -> new PageImpl<>(
            Collections.singletonList(cPage),
            invocation.getArgumentAt(4, Pageable.class), 1));
    // Mark expired pages as deleted.
    when(pageRepository
        .markExpiredWebPagesAsDeleted(eq(1), eq("product"), any(Date.class),
            any(Date.class))).thenReturn(13);
    // SolrClient add
    when(solrClient.add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class)))
        .thenReturn(new UpdateResponse());

    converterService.process(1, false);

    assertThat(converterService.getPagesAdded(), is(0));
    assertThat(converterService.getPagesExpired(), is(13));
    assertThat(converterService.getPagesUpdated(), is(1));
    assertThat(converterService.getPagesDeleted(), is(0));

    verify(webSiteRepository, times(1)).findOne(eq(1));
    verify(webSiteRepository, times(1)).save(webSite);

    verify(pageRepository, times(1)).findModifiedWebPages(eq(1), eq("product"),
        eq(lastCleanedUpAt), eq(true), any(Pageable.class));
    verify(pageRepository, times(1)).findModifiedWebPages(eq(1), eq("product"),
        eq(lastProcessedAt), eq(false), any(Pageable.class));
    verify(pageRepository, times(1))
        .markExpiredWebPagesAsDeleted(eq(1), eq("product"), any(Date.class), any(Date.class));
    verify(pageRepository, times(1))
        .save(anyListOf(CPage.class));

    verify(solrClient, times(0))
        .deleteById(eq("manufacturer_product"), anyListOf(String.class));
    verify(solrClient, times(1))
        .add(eq("manufacturer_product"), anyListOf(SolrInputDocument.class));
    verify(solrClient, times(2))
        .commit(eq("manufacturer_product"));
  }
}