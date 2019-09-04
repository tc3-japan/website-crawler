package com.topcoder.productsearch.api.securities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  /**
   * is enabled auth
   */
  @Value("${authentication.enabled}")
  private Boolean authEnabled;

  @Value("${authentication.credentials.username}")
  private String userName;

  @Value("${authentication.credentials.password}")
  private String password;

  private Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    logger.info("auth enabled = " + (authEnabled ? "true" : "false"));
    if (authEnabled) {
      http
          .csrf().disable()
          .authorizeRequests().anyRequest().authenticated()
          .and()
          .httpBasic();
    }
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth)
      throws Exception {
    if (authEnabled) {
      auth.inMemoryAuthentication()
          .withUser(userName)
          .password(password)
          .roles("USER");
    }
  }
}
