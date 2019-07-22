package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.DestinationURL;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository defines operations on DestinationURL entity.
 */
@Repository
public interface DestinationURLRepository extends CrudRepository<DestinationURL, Integer> {
  DestinationURL findByUrl(String url);
}
