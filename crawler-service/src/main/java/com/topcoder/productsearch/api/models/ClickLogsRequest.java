package com.topcoder.productsearch.api.models;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

/**
 * the click logs Request class
 */
@Data
@ToString
public class ClickLogsRequest {

  /**
   * the search id
   */
  @JsonProperty("search_id")
  private String searchId;

  /**
   * the search words
   */
  @NotNull
  @JsonProperty("search_words")
  private String searchWords;

  /**
   * the page url
   */
  @NotNull
  @JsonProperty("page_url")
  private String pageUrl;

  /**
   * the page rank
   */
  @JsonProperty("page_rank")
  private Integer pageRank;
}
