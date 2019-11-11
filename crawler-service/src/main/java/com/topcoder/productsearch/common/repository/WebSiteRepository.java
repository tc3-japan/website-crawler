package com.topcoder.productsearch.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.topcoder.productsearch.common.entity.WebSite;

/**
 * The repository defines operations on WebSite entity.
 */
@Repository
public interface WebSiteRepository extends CrudRepository<WebSite, Integer> {
  WebSite findByDeletedAndId(Boolean deleted, Integer id);

  @Query("select p from WebSite p where (p.deleted = :deleted) and (p.name like %:keyword% or p.description like %:keyword%)")
  Page<WebSite> findWebSitesWithQuery(@Param("deleted") Boolean deleted, @Param("keyword") String keyword, Pageable pageable);

  Page<WebSite> findByDeleted(Boolean deleted, Pageable pageable);
}
