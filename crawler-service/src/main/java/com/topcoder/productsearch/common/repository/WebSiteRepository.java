package com.topcoder.productsearch.common.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.topcoder.productsearch.common.entity.WebSite;

/**
 * The repository defines operations on WebSite entity.
 */
@Repository
public interface WebSiteRepository extends CrudRepository<WebSite, Integer> {

}
