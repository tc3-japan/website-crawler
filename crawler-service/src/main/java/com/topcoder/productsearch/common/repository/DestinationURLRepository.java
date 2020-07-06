package com.topcoder.productsearch.common.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.topcoder.productsearch.common.entity.DestinationURL;

/**
 * The repository defines operations on DestinationURL entity.
 */
@Repository
public interface DestinationURLRepository extends CrudRepository<DestinationURL, Integer> {
  List<DestinationURL> findByUrlAndPageId(String url, Integer pageId);
}
