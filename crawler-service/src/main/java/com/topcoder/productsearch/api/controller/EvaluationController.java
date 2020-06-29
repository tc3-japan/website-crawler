package com.topcoder.productsearch.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.topcoder.productsearch.api.models.EvaluateRequest;
import com.topcoder.productsearch.api.models.EvaluationResult;
import com.topcoder.productsearch.opt_evaluate.service.SOEvaluateService;

import lombok.RequiredArgsConstructor;

/**
 * Evaluation Controller class
 */
@RestController
@RequestMapping("/evaluate")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EvaluationController {

  /**
   * search opt evaluate service
   */
  @Autowired
  SOEvaluateService soEvaluateService;

  @PostMapping()
  public EvaluationResult evaluate(@Valid @RequestBody EvaluateRequest request) throws Exception {

    return soEvaluateService.evaluate(request);
  }
}
