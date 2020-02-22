package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.SOEvaluation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository defines operations on SOEvaluation entity.
 */
@Repository
public interface SOEvaluationRepository extends CrudRepository<SOEvaluation, Integer> {
}
