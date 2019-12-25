package com.topcoder.productsearch.api.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.topcoder.productsearch.api.models.Token;
import com.topcoder.productsearch.common.entity.User;
import com.topcoder.productsearch.common.util.HashGenerator;

@Service
public class AuthService {

  private static Logger logger = LoggerFactory.getLogger(AuthService.class);

  @Value("${authentication.user.salt}")
  private String passwordSalt;

  @Value("${authentication.token.secret}")
  private String secret;

  @Value("${authentication.token.issuer}")
  private String issuer;

  @Value("${authentication.token.audience}")
  private String audience;

  @Value("${authentication.token.expiresIn:36000}")
  private Integer expiresIn;

  public Token createToken(User user) {
    Token token = new Token();
    token.setAccessToken(createJWT(user));
    token.setExpiresIn(this.expiresIn);
    token.setTokenType("Bearer");
    return token;
  }

  public boolean authenticate(User user, String password) {

    if (user == null || StringUtils.isEmpty(password)) {
      throw new IllegalArgumentException("user and password are required.");
    }
    return getHash(password).equals(user.getPassword());
  }

  public void generatePassword(String password) {
    logger.info("============ PASSWORD HASH =============");
    logger.info(getHash(password));
    logger.info("========================================");
  }

  protected String getHash(String secret) {
    HashGenerator gen = new HashGenerator();

    return gen.hash(secret + passwordSalt);
  }

  protected String createJWT(User user) {
    if (this.secret == null) {
      throw new IllegalStateException("secret is missing.");
    }
    Algorithm algo = Algorithm.HMAC256(this.secret.getBytes(StandardCharsets.UTF_8));
    return JWT.create()
        .withIssuer(this.issuer)
        .withSubject(user.getId() != null ? user.getId().toString() : "")
        .withAudience(this.audience)
        .withIssuedAt(new Date())
        .withExpiresAt(new Date(System.currentTimeMillis() + this.expiresIn * 1000))
        .withClaim("username", user.getUsername())
        .withClaim("email", user.getEmail())
        .withArrayClaim("scopes", "ROLE_ADMIN".split(","))
        .sign(algo);
  }

}
