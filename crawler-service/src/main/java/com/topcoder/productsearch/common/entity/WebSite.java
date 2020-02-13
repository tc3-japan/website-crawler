package com.topcoder.productsearch.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * the website entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "web_sites")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class WebSite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name")
  @NotEmpty(message = "name is required")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "url")
  @NotEmpty(message = "url is required")
  private String url;

  @Column(name = "content_url_patterns")
  @NotEmpty(message = "content_url_patterns is required")
  private String contentUrlPatterns;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
  @Column(name = "created_at")
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_at")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
  private Date lastModifiedAt;

  @Column(name = "supports_robots_txt")
  private Boolean supportsRobotsTxt;

  @Min(value = 1, message = "{crawl_max_depth.min}")
  @Column(name = "crawl_max_depth")
  private Integer crawlMaxDepth = 10;

  @Min(value = 1, message = "{crawl_time_limit.min}")
  @Column(name = "crawl_time_limit")
  private Integer crawlTimeLimit = 600;

  @Min(value = 1, message = "{crawl_interval.min}")
  @Column(name = "crawl_interval")
  private Integer crawlInterval = 1000;

  @Min(value = 1, message = "{parallel_size.min}")
  @Column(name = "parallel_size")
  private Integer parallelSize = 12;

  @Column(name = "timeout_page_download")
  private Integer timeoutPageDownload = 2;

  @Column(name = "retry_times")
  private Integer retryTimes = 2;

  @Column(name = "page_expired_period")
  private Integer pageExpiredPeriod = 30;

  @Column(name = "category_extraction_pattern")
  private String categoryExtractionPattern;

  @Column(name = "content_selector")
  private String contentSelector;

  @Column(name = "deleted")
  @JsonIgnore
  private Boolean deleted = Boolean.FALSE;

  /**
   * default search weights
   */
  private Float weight1;
  private Float weight2;
  private Float weight3;
  private Float weight4;
  private Float weight5;
  private Float weight6;
  private Float weight7;
  private Float weight8;
  private Float weight9;
  private Float weight10;
}
