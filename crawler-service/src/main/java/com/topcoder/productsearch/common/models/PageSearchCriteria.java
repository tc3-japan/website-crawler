package com.topcoder.productsearch.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * the page search
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class PageSearchCriteria {

  /**
   * website id
   */
  private Integer websiteId;

  /**
   * deleted flag
   */
  private Boolean deleted;
}
