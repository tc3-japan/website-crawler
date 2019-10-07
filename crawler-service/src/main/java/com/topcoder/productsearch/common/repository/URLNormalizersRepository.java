package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.URLNormalizers;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository defines operations on URLRepository entity.
 */
@Repository
public interface URLNormalizersRepository extends CrudRepository<URLNormalizers, Integer> {
  URLNormalizers findByWebsiteId(Integer websiteId);
}
