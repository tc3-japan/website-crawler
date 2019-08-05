package com.topcoder.productsearch.common.util;


import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.PageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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

  @Test
  public void testRemoveHash() {
    assertEquals("http://test.com?a=b", Common.removeHashFromURL("http://test.com?a=b#ccc"));
    assertEquals("http://test.com?a=b", Common.removeHashFromURL("http://test.com?a=b"));
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
    when(pageRepository.findAll(pageRequest)).thenReturn(springPage);
    when(pageRepository.findAllBySiteId(1, pageRequest)).thenReturn(new LinkedList<>());

    List<CPage> pages = Common.fetch(pageRepository, 1, pageRequest);
    assertEquals(0, pages.size());
    pages = Common.fetch(pageRepository, null, pageRequest);
    assertEquals(0, pages.size());
  }

  @Test
  public void testReadAndProcessPage() throws InterruptedException {
    List<CPage> pages = new LinkedList<>();
    CPage page = new CPage();
    page.setId(1);
    pages.add(page);
    when(pageRepository.findAllBySiteId(1, new PageRequest(0, 4))).thenReturn(pages);
    Common.readAndProcessPage(1, 4, pageRepository, (threadPoolExecutor, cPage) -> {
      threadPoolExecutor.submit(() -> {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
      assertEquals(Integer.valueOf(1), cPage.getId());
    });
  }
}