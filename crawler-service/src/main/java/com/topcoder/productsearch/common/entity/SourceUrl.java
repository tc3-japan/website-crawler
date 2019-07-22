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

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "source_urls")
public class SourceUrl extends Auditable {

  /**
   * url of the source page.
   */
  @Column(name = "url")
  private String url;

  /**
   * Destination of the page.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "page_id")
  private Page destinationPage;
}
