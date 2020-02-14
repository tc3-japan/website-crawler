package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.SOResultDetail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository defines operations on SOResultDetail entity.
 */
@Repository
public interface SOResultDetailRepository extends CrudRepository<SOResultDetail, Integer> {
}
