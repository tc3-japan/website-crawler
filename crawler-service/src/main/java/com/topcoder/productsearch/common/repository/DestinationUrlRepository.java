package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.DestinationUrl;
import com.topcoder.productsearch.common.entity.Page;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

/**
 * JPA repository for {@code DestinationUrl} entity.
 */
public interface DestinationUrlRepository extends CrudRepository<DestinationUrl, Integer> {

  /**
   * Find by sourcePage(page_id).
   *
   * @param sourcePage - the source page object
   * @return a list of {@code DestinationUrl }
   */
  List<DestinationUrl> findBySourcePage(Page sourcePage);
}
