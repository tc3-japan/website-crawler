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
  @Min(value = 0, message = "{productsearch_start.min}")
  private Integer start = 0;

  /**
   * the rows
   */
  @Min(value = 1, message = "{productsearch_rows.min}")
  private Integer rows = 10;

  /**
   * first n characters of content
   */
  @Min(value = 120, message = "productsearch_characters.min")
  private Integer firstNOfContent = 120;
}
