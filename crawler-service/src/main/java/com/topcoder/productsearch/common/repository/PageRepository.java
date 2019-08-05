package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.CPage;
import java.util.Date;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * The repository defines operations on CPage entity.
 */
@Repository
public interface PageRepository extends CrudRepository<CPage, Integer> {

  CPage findByUrl(String url);

  @Query("select p from CPage p "
      + "where p.siteId=:siteId "
      + "and p.type=:type "
      + "and p.deleted=:deleted")
  Page<CPage> findAllWebPages(@Param("siteId") Integer siteId,
      @Param("type") String type, @Param("deleted") boolean deleted, Pageable pageable);

  @Query("select p from CPage p "
      + "where p.siteId=:siteId "
      + "and p.type=:type "
      + "and p.lastModifiedAt>:lastProcessedAt "
      + "and p.deleted=:deleted")
  Page<CPage> findModifiedWebPages(@Param("siteId") Integer siteId,
      @Param("type") String type, @Param("lastProcessedAt") Date lastProcessedAt,
      @Param("deleted") boolean deleted, Pageable pageable);

  @Modifying
  @Query("update CPage p set p.deleted=true, p.lastModifiedAt=:modifiedAt "
      + "where p.siteId=:siteId "
      + "and p.type=:type "
      + "and p.lastModifiedAt<:cutoff "
      + "and p.deleted=false")
  int markExpiredWebPagesAsDeleted(@Param("siteId") Integer siteId,
      @Param("type") String type, @Param("modifiedAt") Date modifiedAt,
      @Param("cutoff") Date cutoff);
}
