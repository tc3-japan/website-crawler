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
  @NotEmpty(message = "description is required")
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

  @Column(name = "crawl_max_depth")
  @NotNull(message = "crawl_max_depth is required")
  @Min(value = 1, message = "crawlMaxDepth must be greater than 1")
  private Integer crawlMaxDepth;

  @Column(name = "crawl_time_limit")
  @NotNull(message = "crawl_time_limit is required")
  @Min(value = 1, message = "parallelSize must be greater than 1")
  private Integer crawlTimeLimit;

  @Column(name = "crawl_interval")
  @NotNull(message = "crawl_interval is required")
  @Min(value = 0, message = "parallelSize must be greater than 0")
  private Integer crawlInterval;

  @Column(name = "parallel_size")
  @NotNull(message = "parallel_size is required")
  @Min(value = 1, message = "parallelSize must be greater than 1")
  private Integer parallelSize;

  @Column(name = "timeout_page_download")
  @NotNull(message = "timeout_page_download is required")
  @Min(value = 1, message = "timeoutPageDownload must be greater than 1")
  private Integer timeoutPageDownload;

  @Column(name = "retry_times")
  @NotNull(message = "retry_times is required")
  @Min(value = 1, message = "retryTimes must be greater than 1")
  private Integer retryTimes;

  @Column(name = "page_expired_period")
  @NotNull(message = "page_expired_period is required")
  @Min(value = 1, message = "pageExpiredPeriod must be greater than 1")
  private Integer pageExpiredPeriod;

  @Column(name = "category_extraction_pattern")
  @NotNull(message = "category_extraction_pattern is required")
  private String categoryExtractionPattern;

  @Column(name = "content_selector")
  @NotNull(message = "content_selector is required")
  private String contentSelector;

  @Column(name = "deleted")
  @JsonIgnore
  private Boolean deleted = Boolean.FALSE;

}
