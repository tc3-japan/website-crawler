package com.topcoder.productsearch.calctr.model;

import lombok.Data;

/**
 * entity used for count the number of records per search_words
 */
@Data
public class ClickLogCount1 {
  private String words;
  private Integer cnt;
}
