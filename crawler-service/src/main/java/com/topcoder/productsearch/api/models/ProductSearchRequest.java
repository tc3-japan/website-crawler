package com.topcoder.productsearch.api.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


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
  @Min(value = 1, message = "productsearch_characters.min")
  private Integer firstNOfContent = 120;

  /**
   * The weight is a number and it represents how the related field is important in the search
   */
  private List<Float> weights;

  /**
   * site id list, default is empty
   */
  @JsonProperty("manufacturer_ids")
  private List<Integer> manufacturerIds = new ArrayList<>();

  /**
   * debug, will return explain
   */
  private boolean debug = false;
}
