package com.topcoder.productsearch.common.model;

import com.topcoder.productsearch.common.entity.WebSite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


/**
 * Crawler task model
 */
@Getter
@Setter
@AllArgsConstructor
public class CrawlerTask {

  public CrawlerTask(String url, WebSite site) {
    this.url = url;
    this.site = site;
  }

  /**
   * the task depth
   */
  private Integer depth = 1;

  /**
   * task startTime
   */
  private Long startTime = System.currentTimeMillis();

  /**
   * the full url
   */
  private String url;

  /**
   * the site
   */
  private WebSite site;
}
