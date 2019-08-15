package com.topcoder.productsearch.api.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * the solr product class
 */
@Data
public class SolrProduct {
  private String id;

  @JsonProperty("manufacturer_name")
  private String manufacturerName;

  private String url;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SS")
  @JsonProperty("last_modified_at")
  private Date lastModifiedAt;

  private Float score;
}
