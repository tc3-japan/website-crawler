package com.topcoder.productsearch.api.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.topcoder.productsearch.api.models.WebSiteSearchRequest;
import com.topcoder.productsearch.api.services.WebSiteService;
import com.topcoder.productsearch.common.entity.WebSite;

import lombok.RequiredArgsConstructor;

/**
 * [Example] Controller class for WebSite entity.
 */
@RestController
@RequestMapping("/websites")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WebSiteController {
  /**
   * the website service
   */
  private final WebSiteService webSiteService;

  /**
   * get website by id
   *
   * @param webSiteId the website id
   * @return the website
   */
  @GetMapping("{webSiteId}")
  public WebSite get(@PathVariable("webSiteId") @NotNull Integer webSiteId) {
    return webSiteService.get(webSiteId);
  }

  /**
   * create new website
   *
   * @param webSite the website id
   * @return the new website
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public WebSite create(@RequestBody @Valid WebSite webSite) {
    return webSiteService.create(webSite);
  }

  /**
   * search website by name and description
   *
   * @param request the website search request
   * @return the list of result
   */
  @GetMapping
  public List<WebSite> search(@Valid WebSiteSearchRequest request) {
    return webSiteService.search(request).getContent();
  }

  /**
   * update website
   *
   * @param webSiteId the website id
   * @param entity    the request entity
   * @return the website
   */
  @PutMapping("{webSiteId}")
  public WebSite update(@PathVariable("webSiteId") @NotNull Integer webSiteId, @RequestBody @Valid WebSite entity) {
    return webSiteService.update(webSiteId, entity);
  }


  /**
   * remove website
   *
   * @param webSiteId the website id
   */
  @DeleteMapping("{webSiteId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(@PathVariable("webSiteId") @NotNull Integer webSiteId) {
    webSiteService.remove(webSiteId);
  }

}