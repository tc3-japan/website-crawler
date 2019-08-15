package com.topcoder.productsearch.crawler.util;


import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.util.Common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;


/**
 * unit test for common class
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class CommonTest {


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
    String url = "https://www.uniqlo.com/us/en/women-airism-slip-alexander-wang-418189.html?dwvar_418189_color=COL09&cgid=women-airism-collection";
    assertEquals(true, common.isMatch(webSite, url));
    assertEquals(false, Common.isMatch(webSite, "http://test.com?a=b"));
  }

  @Test
  public void testNormalize() {
    String url = "https://www.uniqlo.com/us/en/women-airism-slip-alexander-wang-418189.html?dwvar_418189_color=COL09&cgid=women-airism-collection";
    assertEquals("https://www.uniqlo.com/us/en/women-airism-slip-alexander-wang-418189.html?dwvar_418189_color=COL09", Common.normalize(url));
    assertEquals("http://test.com?a=b", Common.normalize("http://test.com?a=b"));
  }

}