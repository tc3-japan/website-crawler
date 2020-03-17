package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.SOResult;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository defines operations on SOResult entity.
 */
@Repository
public interface SOResultRepository extends CrudRepository<SOResult, Integer> {
}
