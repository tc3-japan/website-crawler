package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.SOTruth;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository defines operations on SOTruth entity.
 */
@Repository
public interface SOTruthRepository extends CrudRepository<SOTruth, Integer> {
}
