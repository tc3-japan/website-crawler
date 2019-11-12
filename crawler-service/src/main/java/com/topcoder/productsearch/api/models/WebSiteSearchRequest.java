package com.topcoder.productsearch.api.models;

import lombok.Data;

import javax.validation.constraints.Min;


/**
 * website search request class
 */
@Data
public class WebSiteSearchRequest {
  /**
   * the keyword
   */
  private String query;

  /**
   * the start
   */
  @Min(0)
  private Integer start = 0;

  /**
   * the rows
   */
  @Min(1)
  private Integer rows = 1000;
}
