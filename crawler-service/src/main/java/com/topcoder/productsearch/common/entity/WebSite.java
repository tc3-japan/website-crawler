package com.topcoder.productsearch.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity class that maps to the web_sites table.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "web_sites")
public class WebSite extends Auditable {

  /**
   * Primary Key, auto increment.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  /**
   * Name of the website.
   */
  @Column(name = "name")
  private String name;

  /**
   * Description of the website.
   */
  @Column(name = "description")
  private String description;

  /**
   * url to start crawling.
   */
  @Column(name = "url")
  private String url;

  /**
   * Pattern of url to indicate which pages the crawler should save to the database.
   */
  @Column(name = "content_url_patterns")
  private String contentUrlPatterns;

}

