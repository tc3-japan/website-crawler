package com.topcoder.productsearch.api.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * the solr product class
 */
@Data
public class SolrProduct {
  private String id;

  @JsonProperty("manufacturer_name")
  private String manufacturerName;

  @JsonProperty("manufacturer_id")
  private String manufacturerId;

  private String url;

  private String title;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SS")
  @JsonProperty("last_modified_at")
  private Date lastModifiedAt;

  private Float score;

  private String category;

  private String digest;
  private List<String> highlighting;

  /**
   * the debug explain
   */
  private String explain;
}
