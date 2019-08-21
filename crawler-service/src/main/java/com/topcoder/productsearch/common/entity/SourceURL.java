package com.topcoder.productsearch.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * Source url entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "source_urls")
public class SourceURL {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "url")
  private String url;

  @Column(name = "page_id")
  private Integer pageId;


  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at")
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_at")
  private Date lastModifiedAt;
}
