package com.topcoder.productsearch;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {FlywayAutoConfiguration.class, SolrAutoConfiguration.class})
@ComponentScan({
  "com.topcoder.productsearch.api",
  "com.topcoder.productsearch.common",
  "com.topcoder.productsearch.crawler",
  "com.topcoder.productsearch.converter",
  "com.topcoder.productsearch.cleaner"})
public class Application {

  /**
   * The entry main method.
   *
   * @param args the arguments
   * @throws Exception if any error occurs
   */
  public static void main(String[] args) {
    
    boolean isRestMode = false;
    for (String arg : args) {
      if ("--rest".equalsIgnoreCase(arg)) {
        isRestMode = true;
      }
    }

    new SpringApplicationBuilder(Application.class).web(isRestMode).run(args);
  }
}
