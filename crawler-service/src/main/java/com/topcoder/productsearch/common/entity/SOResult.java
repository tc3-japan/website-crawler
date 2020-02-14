package com.topcoder.productsearch.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "search_opt_results")
public class SOResult {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "site_id")
  private Integer siteId;

  @Column(name = "search_words")
  private String searchWords;

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

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at")
  @CreationTimestamp
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_at")
  @UpdateTimestamp
  private Date lastModifiedAt;
}
