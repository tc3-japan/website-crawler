package com.topcoder.productsearch.converter;

import com.topcoder.productsearch.cleaner.service.CleanerService;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.converter.service.SolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * the convert thread test
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class ConvertThreadTest {

  @Mock
  SolrService solrService;

  @Mock
  PageRepository pageRepository;

  @Mock
  CleanerService cleanerService;


  @Test
  public void testRun() throws IOException, SolrServerException {
    CPage page = new CPage();

    page.setLastProcessedAt(null);
    new ConvertThread(page, solrService, pageRepository, cleanerService).run();
    verify(cleanerService, times(1)).cleanPage(any(CPage.class));

    page.setDeleted(true);
    new ConvertThread(page, solrService, pageRepository, cleanerService).run();
    verify(solrService, times(1)).deleteByURL(any(String.class));

    page.setDeleted(false);
    new ConvertThread(page, solrService, pageRepository, null).run();

    page.setLastProcessedAt(Date.from(Instant.ofEpochMilli(System.currentTimeMillis() - 1000)));
    page.setLastModifiedAt(Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + 1000)));
    new ConvertThread(page, solrService, pageRepository, cleanerService).run();
  }
}
