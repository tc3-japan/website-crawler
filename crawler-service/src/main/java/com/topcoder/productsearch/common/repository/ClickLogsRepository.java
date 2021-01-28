package com.topcoder.productsearch.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.topcoder.productsearch.common.entity.ClickLogs;

/**
 * the repository defines operations on ClickLogs entity.
 */
@Repository
public interface ClickLogsRepository extends JpaRepository<ClickLogs, Integer> {
}
