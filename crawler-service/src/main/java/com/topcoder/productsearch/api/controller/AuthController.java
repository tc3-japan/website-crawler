package com.topcoder.productsearch.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.topcoder.productsearch.api.exceptions.UnauthorizedException;
import com.topcoder.productsearch.api.models.Token;
import com.topcoder.productsearch.api.services.AuthService;
import com.topcoder.productsearch.api.services.UserService;
import com.topcoder.productsearch.common.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class AuthController {

  private static Logger logger = LoggerFactory.getLogger(AuthController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private AuthService authService;

  @PostMapping(path = "/token", consumes = "application/x-www-form-urlencoded", produces = "application/json")
  public Token authenticate(
      @RequestParam(value = "username", required = true) String username,
      @RequestParam(value = "password", required = true) String password) {

    User user = this.userService.findByUsername(username);
    if (user == null) {
      throw new UnauthorizedException("wrong username or password.");
    }
    if (user.getDeleted()) {
      logger.info("User[" + username + "] has been deleted.");
      throw new UnauthorizedException("wrong username or password.");
    }
    if (!authService.authenticate(user, password)) {
      throw new UnauthorizedException("wrong username or password.");
    }

    Token token = this.authService.createToken(user);

    return token;
  }

}
