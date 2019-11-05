package com.topcoder.productsearch.common.util;


import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.models.PageSearchCriteria;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.specifications.PageSpecification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * unit test for common class
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CommonTest {

  @Mock
  PageRepository pageRepository;

  @Mock
  Page springPage;

  @Mock
  Page<CPage> pages;

  @Test
  public void testRemoveHash() {
    assertEquals("http://test.com?a=b", Common.removeHashSymbolFromURL("http://test.com?a=b#ccc"));
    assertEquals("http://test.com?a=b", Common.removeHashSymbolFromURL("http://test.com?a=b"));
  }

  @Test
  public void testIsMatch() {
    Common common = new Common();
    WebSite webSite = new WebSite();
    webSite.setContentUrlPatterns("https://www.uniqlo.com/us/en/[^/]+?.html.*?cgid=.*?$");
    String url = "https://www.uniqlo.com/us/en/women-airism-slip" +
        "-alexander-wang-418189.html?dwvar_418189_color=COL09&cgid=women-airism-collection";
    assertEquals(true, common.isMatch(webSite, url));
    assertEquals(false, Common.isMatch(webSite, "http://test.com?a=b"));
  }

  @Test
  public void testNormalize() {
    String url = "https://www.uniqlo.com/us/en/women-airism-slip-alexander" +
        "-wang-418189.html?dwvar_418189_color=COL09&cgid=women-airism-collection";
    assertEquals("https://www.uniqlo.com/us/en/women-airism-" +
        "slip-alexander-wang-418189.html?dwvar_418189_color=COL09", Common.normalize(url));
    assertEquals("http://test.com?a=b", Common.normalize("http://test.com?a=b"));
  }

  @Test
  public void testIsUrlBroken() {
    assertEquals(true, Common.isUrlBroken("???"));
    assertEquals(true, Common.isUrlBroken("http://google.com/a/cpanel/a/a/b.html"));
    assertEquals(false, Common.isUrlBroken("http://google.com"));
    assertEquals(false, Common.isUrlBroken("http://unkown-host-zz.com"));
  }

  @Test
  public void testFetch() {
    PageRequest pageRequest = new PageRequest(0, 1);
    when(springPage.getContent()).thenReturn(new LinkedList());
    when(pageRepository.findAll(any(PageSpecification.class), any(Pageable.class))).thenReturn(pages);

    List<CPage> pages = Common.fetch(pageRepository, new PageSearchCriteria(1, null), pageRequest);
    assertEquals(0, pages.size());
    pages = Common.fetch(pageRepository, new PageSearchCriteria(1, true), pageRequest);
    assertEquals(0, pages.size());
  }

  @Test
  public void testReadAndProcessPage() throws InterruptedException {
    List<CPage> pageList = new LinkedList<>();
    when(pages.getContent()).thenReturn(pageList);
    when(pageRepository.findAll(any(PageSpecification.class), any(Pageable.class))).thenReturn(pages);
    Common.readAndProcessPage(new PageSearchCriteria(1, null), 4, pageRepository,
        (threadPoolExecutor, cPage1) -> {
      threadPoolExecutor.submit(() -> {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
      verify(pageRepository, times(1)).findAll(any(PageSpecification.class), any(Pageable.class));
    });
  }

  // @Test
  // public void testHasAccess() {
  //   WebSite site = new WebSite();
  //   site.setUrl("https://www.uniqlo.com/us/en");
  //   assertEquals(false, Common.hasAccess(site, "https://www.uniqlo.com/us/en/search"));
  //   assertEquals(true, Common.hasAccess(site, "https://www.uniqlo.com/us/en/test.html"));
  // }
  
  @Test
  public void testIsUnnecessary() {
    assertEquals(true, Common.isUnnecessary("https://www.ikea.com/furniture-dealer.pdf"));
    assertEquals(true, Common.isUnnecessary(null));
    assertEquals(false, Common.isUnnecessary("/women/plants"));
    assertEquals(false, Common.isUnnecessary("http://test.com/a.html"));
  }

  @Test
  public void testEndsWithHTML() {
    assertEquals(true, Common.endsWithHTML("http://hello.html"));
    assertEquals(false, Common.endsWithHTML(".html.not"));
    assertEquals(false, Common.endsWithHTML("/us/en/men-u-crew-neck-short-sleeve-t-shirt-414351.html?" +
    "dwvar_414351_color=COL46&cgid=men-wear-to-work"));
  }

  @Test
  public void testFirstOfString() {
    assertEquals("test", Common.firstNOfString("test", 4));
    assertEquals("hello ...", Common.firstNOfString("hello world", 7));
  }
}