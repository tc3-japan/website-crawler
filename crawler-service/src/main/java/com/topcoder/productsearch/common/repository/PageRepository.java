package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.specifications.PageSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository defines operations on CPage entity.
 */
@Repository
public interface PageRepository extends CrudRepository<CPage, Integer>, JpaSpecificationExecutor<CPage> {

  /**
   * find by url
   *
   * @param url the url
   * @return the page entity
   */
  CPage findByUrl(String url);
}
