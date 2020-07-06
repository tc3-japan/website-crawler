package com.topcoder.productsearch.common.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.topcoder.productsearch.common.entity.SourceURL;

/**
 * The repository defines operations on SourceURL entity.
 */
@Repository
public interface SourceURLRepository extends CrudRepository<SourceURL, Integer> {
  List<SourceURL> findByUrlAndPageId(String url, Integer pageId);
}
