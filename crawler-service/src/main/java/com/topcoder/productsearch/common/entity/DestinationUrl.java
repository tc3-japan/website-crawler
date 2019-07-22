package com.topcoder.productsearch.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The destination url entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "destination_urls")
public class DestinationUrl extends Auditable {

  /**
   * Destination url.
   */
  @Column(name = "url")
  private String url;

  /**
   * Id of the source page.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "page_id")
  private Page sourcePage;

}
