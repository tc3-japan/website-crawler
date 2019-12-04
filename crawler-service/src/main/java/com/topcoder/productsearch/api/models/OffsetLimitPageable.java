package com.topcoder.productsearch.api.models;

import org.springframework.data.domain.PageRequest;

/**
 * offset limit pageable request
 */
public class OffsetLimitPageable extends PageRequest {

  /**
   * the offset value
   */
  private int start;

  public OffsetLimitPageable(int start, int rows) {
    super(start, rows);
    this.start = start;
  }

  /**
   * get the offset
   *
   * @return the start value
   */
  @Override
  public int getOffset() {
    return this.start;
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
