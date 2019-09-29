package com.topcoder.productsearch.cleaner.service;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * the ValidatePages  service unit test
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class ValidatePagesServiceTest extends AbstractUnitTest {

  @Mock
  PageRepository pageRepository;

  @Mock
  WebSiteRepository webSiteRepository;

  @InjectMocks
  ValidatePagesService validatePagesService;

  @Mock
  Page<CPage> pages;

  WebSite webSite = createWebSite();

  @Before
  public void init() {
    webSite.setParallelSize(4);
    webSite.setPageExpiredPeriod(10);
    when(webSiteRepository.findOne(anyInt())).thenReturn(webSite);
  }


  @Test
  public void testValidatePages() throws InterruptedException {
    when(pages.getContent()).thenReturn(new LinkedList<>());
    when(pageRepository.findAll(any(PageSpecification.class), any(Pageable.class))).thenReturn(pages);

    validatePagesService.validate(1);
    verify(pageRepository, times(1)).findAll(any(PageSpecification.class), any(Pageable.class));

    CPage cPage = new CPage();
    cPage.setUrl("http://google.com/a/a/a/a/b.html");
    when(pageRepository.save(any(CPage.class))).thenReturn(cPage);
    validatePagesService.process(cPage);
    verify(pageRepository, times(1)).save(any(CPage.class));
    assertTrue(cPage.getDeleted());
  }
}
