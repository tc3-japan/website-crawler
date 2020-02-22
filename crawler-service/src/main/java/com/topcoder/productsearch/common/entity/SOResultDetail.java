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
@Table(name = "search_opt_result_details")
public class SOResultDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "result_id")
  private Integer resultId;

  @Column(name = "`rank`")
  private Integer rank;

  private String url;

  private String title;

  private Float score;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at")
  @CreationTimestamp
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_at")
  @UpdateTimestamp
  private Date lastModifiedAt;
}
