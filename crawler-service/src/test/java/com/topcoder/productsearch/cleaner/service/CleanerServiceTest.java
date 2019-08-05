package com.topcoder.productsearch.cleaner.service;

import com.topcoder.productsearch.AbstractUnitTest;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.converter.service.SolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * the cleaner service unit test
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CleanerServiceTest extends AbstractUnitTest {

  @Mock
  PageRepository pageRepository;

  @Mock
  SolrService solrService;

  @InjectMocks
  CleanerService cleanerService;


  @Test
  public void testClean() throws InterruptedException {

    cleanerService.setParallelSize(4);
    List<CPage> pages = new LinkedList<>();
    CPage page = new CPage();
    page.setId(1);
    pages.add(page);

    Pageable pageable = new PageRequest(0, 4);
    when(pageRepository.findAllBySiteId(1, pageable)).thenReturn(pages);
    cleanerService.clean(1);
    verify(pageRepository, times(2)).findAllBySiteId(any(Integer.class), any(Pageable.class));
  }

  @Test
  public void testCleanPage() throws IOException, SolrServerException {
    CPage cPage = new CPage();
    cPage.setUrl("http://google.com/a/a/a/a/b.html");
    cleanerService.setPageExpiredPeriod(10L);
    cPage.setLastModifiedAt(java.sql.Date.valueOf(LocalDate.now().minusDays(12L)));
    cleanerService.cleanPage(cPage);
    verify(solrService, times(1)).deleteByURL(any(String.class));

    cleanerService.setPageExpiredPeriod(10L);
    cPage.setLastModifiedAt(Date.from(Instant.now()));
    cleanerService.cleanPage(cPage);
    verify(solrService, times(2)).deleteByURL(any(String.class));

    cleanerService.setSolrService(null);
    try {
      cleanerService.cleanPage(cPage);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
