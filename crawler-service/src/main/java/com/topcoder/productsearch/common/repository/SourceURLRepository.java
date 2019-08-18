package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.SourceURL;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository defines operations on SourceURL entity.
 */
@Repository
public interface SourceURLRepository extends CrudRepository<SourceURL, Integer> {
  SourceURL findByUrlAndPageId(String url, Integer pageId);
}
