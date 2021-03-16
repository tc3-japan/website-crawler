package com.topcoder.productsearch.calctr.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * entity used for count the number of records per search_words and page_url with the latest click_date in aggregated records
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClickLogCount2 extends ClickLogCount1 {
  private String url;
  private String lastClickDate;
  private float ctr = 0;
}
