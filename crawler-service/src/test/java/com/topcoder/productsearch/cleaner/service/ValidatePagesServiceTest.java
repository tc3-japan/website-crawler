package com.topcoder.productsearch.cleaner.service;

import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.specifications.PageSpecification;
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
public class ValidatePagesServiceTest {

  @Mock
  PageRepository pageRepository;


  @InjectMocks
  ValidatePagesService validatePagesService;

  @Mock
  Page<CPage> pages;

  @Test
  public void testValidatePages() throws InterruptedException {

    validatePagesService.setParallelSize(4);
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
