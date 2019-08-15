package com.topcoder.productsearch.api.models;

import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;


/**
 * product search request class
 */
@Data
public class ProductSearchRequest {

  /**
   * the keywords
   */
  private List<String> query;

  /**
   * the start
   */
  @Min(1)
  private Integer start = 1;

  /**
   * the rows
   */
  @Min(1)
  private Integer rows = 10;
}
