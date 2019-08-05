package com.topcoder.productsearch.converter.config;

import lombok.Setter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration class for creating a SolrClient bean.
 */
@Configuration
@ConfigurationProperties("converter-settings.solr")
public class SolrClientConfiguration {
  /**
   * Solr Server base url.
   */
  @Setter
  private String baseUrl;

  @Bean
  public SolrClient solrClient() {
    return new ConcurrentUpdateSolrClient.Builder(baseUrl).build();
  }
}
