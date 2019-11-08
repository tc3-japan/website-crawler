package com.topcoder.productsearch.common.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * the website entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "web_sites")
public class WebSite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "url")
  private String url;

  @Column(name = "content_url_patterns")
  private String contentUrlPatterns;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at")
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_at")
  private Date lastModifiedAt;

  @Column(name = "supports_robots_txt")
  private Boolean supportsRobotsTxt;

  @Column(name = "crawl_max_depth")
  private Integer crawlMaxDepth;

  @Column(name = "crawl_time_limit")
  private Integer crawlTimeLimit;

  @Column(name = "crawl_interval")
  private Integer crawlInterval;

  @Column(name = "parallel_size")
  private Integer parallelSize;

  @Column(name = "timeout_page_download")
  private Integer timeoutPageDownload;

  @Column(name = "retry_times")
  private Integer retryTimes;

  @Column(name = "page_expired_period")
  private Integer pageExpiredPeriod;

  @Column(name = "category_extraction_pattern")
  private String categoryExtractionPattern;

  @Column(name = "content_selector")
  private String contentSelector;

  @Column(name = "deleted")
  private Boolean deleted = Boolean.FALSE;
}
