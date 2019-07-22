package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.Page;
import org.springframework.data.repository.CrudRepository;

/**
 * JPA Repository for the {@code Page} entity.
 */
public interface PageRepository extends CrudRepository<Page, Integer> {

  /**
   * Find the distinct page by its url.
   *
   * @param url - url of the page.
   * @return {@code Page} object
   */
  Page findDistinctByUrl(String url);
}
