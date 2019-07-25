package com.topcoder.productsearch.converter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConverterService {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(ConverterService.class);

  public ConverterService() {
    logger.debug("Cjust debugging.");
  }

}
