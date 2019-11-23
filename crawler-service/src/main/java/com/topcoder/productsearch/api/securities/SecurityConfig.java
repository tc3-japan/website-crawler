package com.topcoder.productsearch.api.securities;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.auth0.spring.security.api.JwtWebSecurityConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

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

  /**
   * is enabled basic-authentication
   */
  @Value("${authentication.basic.enabled}")
  private Boolean basicAuthEnabled;

  /**
   * username for basic-authentication
   */
  @Value("${authentication.basic.credentials.username}")
  private String userName;

  /**
   * password for basic-authentication
   */
  @Value("${authentication.basic.credentials.password}")
  private String password;

  /**
   * is enabled token-based-authentication
   */
  @Value("${authentication.token.enabled}")
  private Boolean tokenAuthEnabled;

  /**
   * The issuer of tokens
   */
  @Value("${authentication.token.issuer}")
  private String issuer;

  /**
   * The audience of tokens
   */
  @Value("${authentication.token.audience}")
  private String audience;

  /**
   * The secret to verify token
   */
  @Value("${authentication.token.secret}")
  private String secret;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    logger.info("basic authentication enabled = " + (basicAuthEnabled ? "true" : "false"));

    CorsConfigurationSource cors = (HttpServletRequest request) -> {
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowedHeaders(Collections.singletonList("*"));
      config.setAllowedOrigins(Arrays.asList(corsAllowedOrigins.split(",")));
      config.setAllowedMethods(Arrays.asList(corsAllowedMethods.split(",")));
      config.setAllowCredentials(true);
      return config;
    };
    http.csrf().disable()
        .cors().configurationSource(cors)
        .and()
        .authorizeRequests()
        .mvcMatchers("/token").permitAll();

    if (tokenAuthEnabled) {
      JwtWebSecurityConfigurer.forHS256(audience, issuer, secret.getBytes(StandardCharsets.UTF_8))
          .configure(http)
          .authorizeRequests()
          .antMatchers("/api/websites/**").authenticated();
    }
    if (basicAuthEnabled) {
      http.authorizeRequests()
          .mvcMatchers("/search_products").authenticated()
          .and()
          .httpBasic();
    }
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth)
      throws Exception {
    if (basicAuthEnabled) {
      auth.inMemoryAuthentication()
          .withUser(userName)
          .password(password)
          .roles("USER");
    }
  }
}
