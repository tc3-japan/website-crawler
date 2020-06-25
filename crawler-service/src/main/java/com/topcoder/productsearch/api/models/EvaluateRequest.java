package com.topcoder.productsearch.api.models;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

/**
 * Evaluate Request class
 */
@Data
@ToString
public class EvaluateRequest {

  /**
   * The weights
   */
  private List<Float> weights;

  /**
   * the beginning row of the truth data used in the evaluation
   */
  @NotNull
  @JsonProperty("start_truth_id")
  private Integer startTruthId = 0;

  /**
   * the size of truth data used in the evaluation
   */
  @Min(value = 1)
  private Integer size = 1;

  /**
   * the type of the query (standard | dismax)
   */
  @JsonProperty("query_type")
  private String queryType;

  /**
   * the flag to indicate the evaluator to save results in the database.
   */
  @JsonProperty("save_result")
  private boolean saveResult  = false;
}
