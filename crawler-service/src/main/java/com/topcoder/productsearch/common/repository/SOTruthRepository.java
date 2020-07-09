package com.topcoder.productsearch.common.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.topcoder.productsearch.common.entity.SOTruth;

/**
 * The repository defines operations on SOTruth entity.
 */
@Repository
public interface SOTruthRepository extends CrudRepository<SOTruth, Integer> {

  @Query("select t from SOTruth t where t.siteId = :siteId and t.invalid = false and t.id >= :headId")
  List<SOTruth> findFrom(@Param("siteId") Integer siteId, @Param("headId")Integer headId, Pageable pageable);
}
