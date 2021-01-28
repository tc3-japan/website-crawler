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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * the click logs entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "click_logs")
public class ClickLogs {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "search_id")
  private String searchId;

  @Column(name = "search_words")
  private String searchWords;

  @Column(name = "normalized_search_words")
  private String normalizedSearchWords;

  @Column(name = "page_url")
  private String pageUrl;

  @Column(name = "page_rank")
  private Integer pageRank;

  @Column(name = "created_date")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date createdDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_at")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
  private Date lastModifiedAt;

}
