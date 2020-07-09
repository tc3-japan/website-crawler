package com.topcoder.productsearch.api.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

/**
 * Evaluation Result class
 */
@Data
@ToString
public class EvaluationResult {

  /**
   * The weights as an evaluation target
   */
  private List<Float> weights;

  @JsonProperty("data_count")
  private Integer dataCount = 0;

  @JsonProperty("error_count")
  private Integer errorCount = 0;

  @JsonProperty("score_mean")
  private Float scoreMean;

  @JsonProperty("score_max")
  private Float scoreMax;

  @JsonProperty("score_min")
  private Float scoreMin;

  @JsonProperty("score_median")
  private Float scoreMedian;

  @JsonProperty("score_variance")
  private Float scoreVariance;

  @JsonProperty("score_std_deviation")
  private Float scoreStandardDeviation;

  @JsonProperty("scores")
  private Map<Integer, Float> scores;

  @JsonProperty("elapsed_time_seconds")
  private Integer elapsedTimeSeconds = 0;

}
