package com.topcoder.productsearch.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.topcoder.productsearch.api.models.ClickLogsRequest;
import com.topcoder.productsearch.api.services.ClickLogsService;

import lombok.RequiredArgsConstructor;

/**
 * the clic logs controller class
 */
@RestController
@RequestMapping("/click_logs")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ClickLogsController {

  /**
   * the click logs service
   */
  @Autowired
  private ClickLogsService clickLogsService;

  /**
   * create new click logs
   *
   * @param request the click logs request
   * @return result message
   */
  @PostMapping()
  @ResponseStatus(HttpStatus.OK)
  public String create(@Valid @RequestBody ClickLogsRequest request) {
    return clickLogsService
      .create(request.getSearchId(), 
              request.getSearchWords(),
              request.getPageUrl(),
              request.getPageRank());
  }

}