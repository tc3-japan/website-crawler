package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.SOTruthDetail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository defines operations on SOTruthDetail entity.
 */
@Repository
public interface SOTruthDetailRepository extends CrudRepository<SOTruthDetail, Integer> {
  List<SOTruthDetail> findByTruthId(Integer truthId);
}
