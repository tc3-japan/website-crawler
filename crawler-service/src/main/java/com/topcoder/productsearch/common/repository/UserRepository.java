package com.topcoder.productsearch.common.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.topcoder.productsearch.common.entity.User;

/**
 * The repository defines operations on User entity.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

  User findByUsername(String username);
}
