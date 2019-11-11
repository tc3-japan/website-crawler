package com.topcoder.productsearch.api.controller;

import com.topcoder.productsearch.api.models.WebSiteSearchRequest;
import com.topcoder.productsearch.api.services.WebSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.topcoder.productsearch.api.exceptions.NotFoundException;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;

import lombok.RequiredArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

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
  public WebSite update(@PathVariable("webSiteId") @NotNull Integer webSiteId, @RequestBody WebSite entity) {
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