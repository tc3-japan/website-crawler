package com.topcoder.productsearch.common.entity;

import java.util.Date;
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
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
@Entity
@Table(name = "pages")
public class Page extends Auditable {

  /**
   * url of the page.
   */
  @Column(name = "url")
  private String url;

  /**
   * Web site to which the page belongs.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "site_id")
  @ToString.Exclude
  private WebSite webSite;

  /**
   * Type of the page, always "Product" now.
   */
  @Column(name = "type")
  private String type;

  /**
   * Title of the HTML page.
   */
  @Column(name = "title")
  private String title;

  /**
   * Body of the HTML page.
   */
  @Column(name = "body")
  @ToString.Exclude
  private String body;

  /**
   * E-Tag value obtained from the last download.
   */
  @Column(name = "etag")
  private String etag;

  /**
   * Last Modified value obtained from the last download.
   */
  @Column(name = "last_modified")
  private Date lastModified;
}
