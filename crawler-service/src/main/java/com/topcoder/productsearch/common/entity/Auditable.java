package com.topcoder.productsearch.common.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base class for entities that contains auditing information.
 * <p>Like {@code createdAt} and {@codelastModifiedAt}.</p>
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

  /**
   * Primary Key, auto-incremental.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  /**
   * Create timestamp.
   */
  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_at")
  private Date createdAt;

  /**
   * Last modify timestamp.
   */
  @LastModifiedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_modified_at")
  private Date lastModifiedAt;
}
