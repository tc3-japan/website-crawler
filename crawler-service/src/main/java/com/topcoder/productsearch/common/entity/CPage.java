package com.topcoder.productsearch.common.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * Crawler page, add C in page to avoid same name wit html unit page
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pages")
public class CPage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "url")
  private String url;

  @Column(name = "site_id")
  private Integer siteId;

  @Column(name = "type")
  private String type;

  @Column(name = "title")
  private String title;

  @Column(name = "body")
  private String body;

  @Column(name = "etag")
  private String etag;

  @Column(name = "last_modified")
  private String lastModified;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at")
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_at")
  private Date lastModifiedAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_processed_at")
  private Date lastProcessedAt;

  @Column(name = "deleted")
  private Boolean deleted = Boolean.FALSE;
}
