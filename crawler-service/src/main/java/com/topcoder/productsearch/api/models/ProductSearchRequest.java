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
  @Min(0)
  private Integer start = 0;

  /**
   * the rows
   */
  @Min(1)
  private Integer rows = 10;

  /**
   * first n characters of content
   */
  @Min(120)
  private Integer firstNOfContent = 120;
}
