package com.topcoder.productsearch;

import com.topcoder.productsearch.api.securities.RestCondition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

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
      RestCondition.setRest(isRestMode);
    }
    new SpringApplicationBuilder(Application.class).web(isRestMode).run(args);
  }

  @Bean
  public MessageSource messageSource() {
      ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
      messageSource.setBasename("classpath:messages");
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
  }

  @Bean
  public LocalValidatorFactoryBean getValidator() {
      LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
      bean.setValidationMessageSource(messageSource());
      return bean;
  }
}
