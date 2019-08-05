package com.topcoder.productsearch.converter.service;

import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.repository.PageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * the converter service unit tests
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class ConverterServiceTest {

  @Mock
  PageRepository pageRepository;

  @InjectMocks
  ConverterService converterService;


  @Test
  public void convertTest() throws InterruptedException {
    converterService.setParallelSize(4);
    List<CPage> pages = new LinkedList<>();
    CPage page = new CPage();
    page.setId(1);
    pages.add(page);
    when(pageRepository.findAllBySiteId(1, new PageRequest(0, 4))).thenReturn(pages);
    converterService.convert(1);
    verify(pageRepository, times(2)).findAllBySiteId(any(Integer.class), any(Pageable.class));
  }
}
