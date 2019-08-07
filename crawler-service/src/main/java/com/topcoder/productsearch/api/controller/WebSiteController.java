package com.topcoder.productsearch.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.topcoder.productsearch.api.exceptions.NotFoundException;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;

import lombok.RequiredArgsConstructor;

/**
 * [Example] Controller class for WebSite entity.
 */
@RestController
@RequestMapping("/websites")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class WebSiteController {
  
  //@Autowired
  private final WebSiteRepository webSiteRepository;
  
  @GetMapping("{webSiteId}")
  public WebSite getWebSite(@PathVariable("webSiteId") Integer webSiteId) {
    
    if (webSiteId == null) {
      //TODO: should generate BadRequest error 
    }
    
    WebSite website = webSiteRepository.findOne(webSiteId);
    
    if (website == null) {
      throw new NotFoundException("No WebSite exists.");
    }
    
    return website;
  }
}