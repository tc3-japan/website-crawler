package com.topcoder.productsearch.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "search_opt_truth_details")
public class SOTruthDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "truth_id")
  private Integer truthId;

  @Column(name = "`rank`")
  private Integer rank;

  @Column()
  private String url;

  @Column()
  private String title;

  @Column()
  private Float score;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at")
  @CreationTimestamp
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_at")
  @UpdateTimestamp
  private Date lastModifiedAt;

  /**
   * similarity scores from solr
   */
  @Column(name = "sim_area1")
  private Float simArea1;
  @Column(name = "sim_area2")
  private Float simArea2;
  @Column(name = "sim_area3")
  private Float simArea3;
  @Column(name = "sim_area4")
  private Float simArea4;
  @Column(name = "sim_area5")
  private Float simArea5;
  @Column(name = "sim_area6")
  private Float simArea6;
  @Column(name = "sim_area7")
  private Float simArea7;
  @Column(name = "sim_area8")
  private Float simArea8;
  @Column(name = "sim_area9")
  private Float simArea9;
  @Column(name = "sim_area10")
  private Float simArea10;
}
