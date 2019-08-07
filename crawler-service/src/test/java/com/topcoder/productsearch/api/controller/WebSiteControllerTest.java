package com.topcoder.productsearch.api.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.topcoder.productsearch.api.exceptions.NotFoundException;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;

public class WebSiteControllerTest {
  
  @Test
  public void testGetWebSite() {
    // data
    int webSiteId = 1;
    WebSite website = new WebSite();
    website.setId(webSiteId);
    
    // mock
    WebSiteRepository webSiteRepository = mock(WebSiteRepository.class);
    doReturn(website).when(webSiteRepository).findOne(webSiteId);
    
    // test
    WebSiteController controller = new WebSiteController(webSiteRepository);
    WebSite result = controller.getWebSite(webSiteId);
    
    // check
    assertNotNull(result);
    assertEquals(website.getId(), result.getId());
    verify(webSiteRepository).findOne(webSiteId);
  }
  
  @Test
  public void testGetWebSite_NotFound() {
    // data
    int notExistingId = 1;

    // mock
    WebSiteRepository webSiteRepository = mock(WebSiteRepository.class);
    doReturn(null).when(webSiteRepository).findOne(notExistingId); // not exist
    
    // test
    try {
      WebSiteController controller = new WebSiteController(webSiteRepository);
      controller.getWebSite(notExistingId);
      fail("NotFoundException should be thrown in the previous step.");
    } catch (NotFoundException e) {
    }
    
    // check
    verify(webSiteRepository).findOne(notExistingId);
  }
}