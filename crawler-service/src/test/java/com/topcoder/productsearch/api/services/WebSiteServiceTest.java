package com.topcoder.productsearch.api.services;

import com.topcoder.productsearch.api.models.OffsetLimitPageable;
import com.topcoder.productsearch.api.models.WebSiteSearchRequest;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
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
public class WebSiteServiceTest {

  @Mock
  WebSiteRepository webSiteRepository;

  @Mock
  Page<WebSite> webSitePage;

  @InjectMocks
  WebSiteService webSiteService;

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
    when(webSiteRepository.findByDeletedAndId(false, webSiteId)).thenReturn(website);

    // test
    WebSite result = webSiteService.get(webSiteId);
    // check
    assertNotNull(result);
    assertEquals(website.getId(), result.getId());


    when(webSiteRepository.findByDeletedAndId(false, webSiteId)).thenReturn(null);
    try {
      webSiteService.get(webSiteId);
    } catch (Exception e) {
      assertEquals("No WebSite exists.", e.getMessage());
    }

    try {
      webSiteService.get(null);
    } catch (Exception e) {
      assertEquals("WebSite id cannot be null.", e.getMessage());
    }


  }

  @Test
  public void testCreate() {
    WebSite webSite = createWebSite();
    webSite.setName("un");
    when(webSiteRepository.save(webSite)).thenReturn(webSite);
    try {
      webSiteService.create(webSite);
    } catch (Exception e) {
      assertEquals("id should not be present in request body", e.getMessage());
    }

    webSite.setId(null);
    WebSite webSite1 = webSiteService.create(webSite);
    assertEquals(webSite.getId(), webSite1.getId());
    assertEquals("un", webSite1.getName());
    verify(webSiteRepository, times(1)).save(any(WebSite.class));
  }

  @Test
  public void testUpdate() {
    // given
    WebSite webSite = createWebSite();
    webSite.setId(null);
    webSite.setName("un");
    webSite.setUrl("url");
    webSite.setContentUrlPatterns("contentUrlPatterns");

    // when
    when(webSiteRepository.save(webSite)).thenReturn(webSite);
    when(webSiteRepository.findByDeletedAndId(false, 1)).thenReturn(webSite);

    WebSite webSite1 = webSiteService.update(1, webSite);

    // then
    assertEquals(webSite.getId(), webSite1.getId());
    assertEquals("un", webSite1.getName());
    verify(webSiteRepository, times(1)).save(any(WebSite.class));
  }

  @Test
  public void testRemove() {
    WebSite webSite = createWebSite();
    webSite.setName("un");
    when(webSiteRepository.save(webSite)).thenReturn(webSite);
    when(webSiteRepository.findByDeletedAndId(false, 1)).thenReturn(webSite);

    webSiteService.remove(1);

    assertEquals(true, webSite.getDeleted());
    assertNotNull(webSite.getLastModifiedAt());
    verify(webSiteRepository, times(1)).save(any(WebSite.class));
  }

  @Test
  public void testSearch() {

    WebSiteSearchRequest request = new WebSiteSearchRequest();
    when(webSiteRepository.findByDeleted(anyBoolean(), any(OffsetLimitPageable.class))).thenReturn(webSitePage);
    when(webSiteRepository.findWebSitesWithQuery(anyBoolean(), anyString(), any(OffsetLimitPageable.class))).thenReturn(webSitePage);
    when(webSitePage.getContent()).thenReturn(new LinkedList<>());

    Page<WebSite> webSites = webSiteService.search(request);

    assertEquals(0, webSites.getContent().size());
    verify(webSiteRepository, times(1)).findByDeleted(anyBoolean(), any(OffsetLimitPageable.class));

    OffsetLimitPageable pageable = new OffsetLimitPageable(request.getStart(), request.getRows());
    assertEquals(pageable.getOffset(), request.getStart().intValue());

    request.setQuery("test");
    webSites = webSiteService.search(request);
    assertEquals(0, webSites.getContent().size());
    verify(webSiteRepository, times(1)).findWebSitesWithQuery(anyBoolean(), anyString(), any(OffsetLimitPageable.class));

  }

}
