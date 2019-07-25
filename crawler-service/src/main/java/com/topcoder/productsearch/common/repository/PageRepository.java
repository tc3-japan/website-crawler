package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.CPage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository defines operations on CPage entity.
 */
@Repository
public interface PageRepository extends CrudRepository<CPage, Integer> {
  CPage findByUrl(String url);
}
