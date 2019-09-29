package com.topcoder.productsearch.converter.service;

import com.topcoder.productsearch.AbstractUnitTest;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.common.specifications.PageSpecification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * the converter service unit tests
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class ConverterServiceTest extends AbstractUnitTest {

  @Mock
  PageRepository pageRepository;

  @Mock
  WebSiteRepository webSiteRepository;

  @InjectMocks
  ConverterService converterService;

  @Mock
  Page<CPage> page;

  WebSite webSite = createWebSite();

  @Before
  public void init() {
    webSite.setParallelSize(4);
    webSite.setPageExpiredPeriod(10);
    when(webSiteRepository.findOne(anyInt())).thenReturn(webSite);
  }


  @Test
  public void convertTest() throws InterruptedException {
    
    List<CPage> pages = new LinkedList<>();
    when(page.getContent()).thenReturn(pages);
    when(pageRepository.findAll(any(PageSpecification.class), any(Pageable.class))).thenReturn(page);
    converterService.convert(1);
    assertEquals(page.getContent().size(), 0);
    verify(pageRepository, times(1)).findAll(any(PageSpecification.class), any(Pageable.class));
  }
}
