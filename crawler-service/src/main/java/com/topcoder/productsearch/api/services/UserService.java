package com.topcoder.productsearch.api.services;

import java.time.Instant;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.topcoder.productsearch.common.entity.User;
import com.topcoder.productsearch.common.repository.UserRepository;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AuthService authService;

  /**
   * find user by username
   *
   * @param username
   * @return
   */
  public User findByUsername(String username) {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("username must be specified.");
    }

    return this.userRepository.findByUsername(username);
  }

  /**
   * update user
   *
   * @param user
   * @return
   */
  public User updateUser(User user) {
    if (user == null) {
      throw new IllegalArgumentException("user must be specified.");
    }

    return this.userRepository.save(user);
  }

  /**
   * update user
   *
   * @param user
   * @return
   */
  public User updatePassword(String username, String rawPassword) {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("username must be specified.");
    }
    if (StringUtils.isBlank(rawPassword)) {
      throw new IllegalArgumentException("rawPassword must be specified.");
    }

    User user = findByUsername(username);
    if (user == null) {
      throw new IllegalArgumentException(String.format("user '%s' is not found", username));
    }

    String hash = this.authService.getHash(rawPassword);
    user.setPassword(hash);
    user.setLastModifiedAt(Date.from(Instant.now()));

    return this.userRepository.save(user);
  }
}
