package com.topcoder.productsearch;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {FlywayAutoConfiguration.class})
@ComponentScan({"com.topcoder.productsearch.crawler"})
public class Application {

  /**
   * The entry main method.
   *
   * @param args the arguments
   * @throws Exception if any error occurs
   */
  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class).web(false).run(args);
  }
}
