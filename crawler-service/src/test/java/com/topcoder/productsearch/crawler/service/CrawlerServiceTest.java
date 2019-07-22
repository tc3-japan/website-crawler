package com.topcoder.productsearch.crawler.service;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.topcoder.productsearch.common.entity.DestinationUrl;
import com.topcoder.productsearch.common.entity.Page;
import com.topcoder.productsearch.common.repository.DestinationUrlRepository;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.crawler.BaseTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Unit tests for {@code CrawlerService}.
 */
public class CrawlerServiceTest extends BaseTest {

  /**
   * Mock Page repository
   */
  @Mock
  private PageRepository pageRepository;

  /**
   * Mock DestinationUrl repository
   */
  @Mock
  private DestinationUrlRepository destinationUrlRepository;

  /**
   * DestinationURL argument captor
   */
  @Captor
  private ArgumentCaptor<List<DestinationUrl>> destinationUrlArgumentCaptor;

  /**
   * {@code CrawlerService} under test
   */
  private CrawlerService crawlerService;


  /**
   * Page to save
   */
  Page page = new Page();

  /**
   * Urls to save
   */
  List<String> urls = Arrays.asList(
      "http://www.uniqlo.com/us/en/a-b.html",
      "http://www.uniqlo.com/us/en/a-c.html");

  {
    page.setUrl("http://www.uniqlo.com/us/en/home/");
  }

  @Before
  public void setUp() {
    crawlerService = new CrawlerService(destinationUrlRepository, pageRepository);
  }

  /**
   * Test normal save flow
   */
  @Test
  public void testSave_Normal() {
    // Set up mocks
    when(pageRepository.save(page)).thenReturn(page);
    when(destinationUrlRepository.findBySourcePage(page)).thenReturn(Collections.emptyList());
    when(destinationUrlRepository.save(anyListOf(DestinationUrl.class)))
        .then(invocation -> invocation.getArgumentAt(0, List.class));

    // Run test code
    crawlerService.save(page, urls);

    verify(pageRepository, times(1)).save(page);
    verify(destinationUrlRepository, times(1)).findBySourcePage(page);
    verify(destinationUrlRepository, times(1)).save(destinationUrlArgumentCaptor.capture());

    List<DestinationUrl> urlsSaved = destinationUrlArgumentCaptor.getValue();
    assertThat(urlsSaved, Matchers.hasSize(2));
    assertThat(urlsSaved.get(0).getSourcePage().getUrl(), Matchers.is(page.getUrl()));
    assertThat(urlsSaved.get(0).getUrl(), Matchers.is(urls.get(0)));
    assertThat(urlsSaved.get(1).getSourcePage().getUrl(), Matchers.is(page.getUrl()));
    assertThat(urlsSaved.get(1).getUrl(), Matchers.is(urls.get(1)));
  }

  /**
   * Save should skip existing urls.
   */
  @Test
  public void testSave_SkippingExistingUrl() {
    // Set up mocks
    when(pageRepository.save(page)).thenReturn(page);
    when(destinationUrlRepository.findBySourcePage(page))
        .thenReturn(Collections.singletonList(new DestinationUrl(urls.get(0), page)));
    when(destinationUrlRepository.save(anyListOf(DestinationUrl.class)))
        .then(invocation -> invocation.getArgumentAt(0, List.class));

    // Run test code
    crawlerService.save(page, urls);

    verify(pageRepository, times(1)).save(page);
    verify(destinationUrlRepository, times(1)).findBySourcePage(page);
    verify(destinationUrlRepository, times(1)).save(destinationUrlArgumentCaptor.capture());

    List<DestinationUrl> urlsSaved = destinationUrlArgumentCaptor.getValue();
    assertThat(urlsSaved, Matchers.hasSize(1));
    assertThat(urlsSaved.get(0).getSourcePage().getUrl(), Matchers.is(page.getUrl()));
    assertThat(urlsSaved.get(0).getUrl(), Matchers.is(urls.get(1)));
  }

  /**
   * Test find page by url
   */
  @Test
  public void testFindByUrl() {
    // Set up mocks
    when(pageRepository.findDistinctByUrl(anyString())).thenReturn(null);
    when(pageRepository.findDistinctByUrl(page.getUrl())).thenReturn(page);

    // Run test code
    Page pageReturned = crawlerService.findByUrl(page.getUrl());
    assertThat(pageReturned.getUrl(), Matchers.is(page.getUrl()));

    assertThat(crawlerService.findByUrl("http://example.com/"), Matchers.nullValue());

    verifyZeroInteractions(destinationUrlRepository);
    verify(pageRepository, times(1)).findDistinctByUrl(page.getUrl());
    verify(pageRepository, times(1)).findDistinctByUrl("http://example.com/");
  }
}