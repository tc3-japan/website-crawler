package com.topcoder.productsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * SpringBoot application class.
 */
@SpringBootApplication(exclude = {FlywayAutoConfiguration.class})
@ComponentScan({"com.topcoder.productsearch.crawler"})
@EnableJpaAuditing
public class Application {

  /**
   * The entry main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    ApplicationContext ctx = new SpringApplicationBuilder(Application.class).web(false).run(args);
    SpringApplication.exit(ctx, () -> 0);
  }
}
