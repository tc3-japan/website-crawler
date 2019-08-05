package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.CPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository defines operations on CPage entity.
 */
@Repository
public interface PageRepository extends CrudRepository<CPage, Integer> {

  /**
   * find by url
   *
   * @param url the url
   * @return the page entity
   */
  CPage findByUrl(String url);

  /**
   * find all with pageable
   *
   * @param pageable the page request
   * @return the list of pages
   */
  Page<CPage> findAll(Pageable pageable);


  /**
   * find all by site id with pageable
   *
   * @param id       the site id
   * @param pageable the page request
   * @return the list of pages
   */
  List<CPage> findAllBySiteId(Integer id, Pageable pageable);
}
