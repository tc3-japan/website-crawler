package com.topcoder.productsearch.api.securities;

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@Conditional(RestCondition.class)
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

  /**
   * The CORS allowed origins.
   */
  @Value(value = "${cors.allowed-origins}")
  private String corsAllowedOrigins;

  /**
   * The CORS allowed methods.
   */
  @Value(value = "${cors.allowed-methods}")
  private String corsAllowedMethods;

  private Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    logger.info("auth enabled = " + (authEnabled ? "true" : "false"));
    http.csrf().disable()
        .cors().configurationSource(
            (HttpServletRequest request) -> {
              CorsConfiguration config = new CorsConfiguration();
              config.setAllowedHeaders(Collections.singletonList("*"));
              config.setAllowedOrigins(Arrays.asList(corsAllowedOrigins.split(",")));
              config.setAllowedMethods(Arrays.asList(corsAllowedMethods.split(",")));
              config.setAllowCredentials(true);
              return config;
            });
    if (authEnabled) {
      http
          .authorizeRequests()
          .mvcMatchers("/search_products").authenticated()
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
