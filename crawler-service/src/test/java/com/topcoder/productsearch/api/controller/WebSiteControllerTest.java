package com.topcoder.productsearch.api.controller;

import com.topcoder.productsearch.api.models.WebSiteSearchRequest;
import com.topcoder.productsearch.api.services.WebSiteService;
import com.topcoder.productsearch.common.entity.WebSite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class WebSiteControllerTest {


  @Mock
  WebSiteService webSiteService;

  @Mock
  Page<WebSite> webSitePage;

  @InjectMocks
  WebSiteController webSiteController;


  private WebSite createWebSite() {
    int webSiteId = 1;
    WebSite website = new WebSite();
    website.setId(webSiteId);
    return website;
  }

  @Test
  public void testGetWebSite() {
    // data
    WebSite website = createWebSite();
    Integer webSiteId = website.getId();
    // mock
    when(webSiteService.get(webSiteId)).thenReturn(website);

    // test
    WebSite result = webSiteController.get(webSiteId);

    // check
    assertNotNull(result);
    assertEquals(website.getId(), result.getId());
    verify(webSiteService).get(webSiteId);
  }

  @Test
  public void testSearch() {
    when(webSiteService.search(any(WebSiteSearchRequest.class))).thenReturn(webSitePage);
    when(webSitePage.getContent()).thenReturn(new LinkedList<>());

    List<WebSite> webSites = webSiteController.search(new WebSiteSearchRequest());
    assertEquals(0, webSites.size());
  }

  @Test
  public void testCreate() {
    WebSite webSite = createWebSite();
    when(webSiteService.create(webSite)).thenReturn(webSite);
    WebSite webSite1 = webSiteController.create(webSite);
    assertEquals(webSite1.getId(), webSite.getId());
    verify(webSiteService, times(1)).create(any(WebSite.class));
  }

  @Test
  public void testUpdate() {
    WebSite webSite = createWebSite();
    when(webSiteService.update(1, webSite)).thenReturn(webSite);

    WebSite webSite1 = webSiteController.update(webSite.getId(), webSite);
    assertEquals(webSite1.getId(), webSite.getId());
    verify(webSiteService, times(1)).update(any(Integer.class), any(WebSite.class));
  }

  @Test
  public void testRemove() {
    doNothing().when(webSiteService).remove(1);
    webSiteController.remove(1);
    verify(webSiteService, times(1)).remove(any(Integer.class));
  }

}