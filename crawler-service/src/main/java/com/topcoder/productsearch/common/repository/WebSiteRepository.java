package com.topcoder.productsearch.common.repository;

import com.topcoder.productsearch.common.entity.WebSite;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for accessing {@code WebSite} entity.
 */
@Repository
public interface WebSiteRepository extends CrudRepository<WebSite, Integer> {

}
