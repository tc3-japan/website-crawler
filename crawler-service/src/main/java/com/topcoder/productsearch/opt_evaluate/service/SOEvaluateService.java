package com.topcoder.productsearch.opt_evaluate.service;

import com.topcoder.productsearch.api.models.ProductSearchRequest;
import com.topcoder.productsearch.api.models.SolrProduct;
import com.topcoder.productsearch.common.entity.*;
import com.topcoder.productsearch.common.repository.SOEvaluationRepository;
import com.topcoder.productsearch.common.repository.SOResultDetailRepository;
import com.topcoder.productsearch.common.repository.SOResultRepository;
import com.topcoder.productsearch.common.repository.SOTruthDetailRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.converter.service.SolrService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Evaluate service
 */
@Service
public class SOEvaluateService {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(SOEvaluateService.class);

  /**
   * slor service
   */
  @Autowired
  SolrService solrService;


  /**
   * search opt result repository
   */
  @Autowired
  SOResultRepository soResultRepository;

  /**
   * search opt result detail repository
   */
  @Autowired
  SOResultDetailRepository soResultDetailRepository;

  /**
   * search opt truth detail repository
   */
  @Autowired
  SOTruthDetailRepository soTruthDetailRepository;

  /**
   * search opt truth evaluation repository
   */
  @Autowired
  SOEvaluationRepository soEvaluationRepository;

  /**
   * number of expected url need search
   */
  @Setter // for unit test
  @Value("${optimization.list_size:10}")
  private Integer listSize;

  /**
   * the search opt evaluate threshold value
   */
  @Setter // for unit test
  @Value("${optimization.priority_rank_threshold:3}")
  Integer evaluateThreshold;

  /**
   * create reference data by scraping the result of Google search with some search words for the specific web site.
   *
   * @param soTruth     the search opt truth
   * @param searchWords the search words
   * @param weights     the search weights
   */
  @Transactional
  public SOEvaluation evaluate(SOTruth soTruth, String searchWords, List<Float> weights) throws Exception {
    String words = searchWords == null ? soTruth.getSearchWords() : searchWords;

    ProductSearchRequest request = new ProductSearchRequest();
    request.setManufacturerIds(Collections.singletonList(soTruth.getSiteId()));
    request.setWeights(weights);
    request.setQuery(Arrays.asList(words.split("\\s+")));
    request.setRows(listSize * 3);

    List<SolrProduct> products = solrService.searchProduct(request);


    // result
    SOResult soResult = new SOResult();
    soResult.setSearchWords(words);
    soResult.setSiteId(soTruth.getSiteId());

    if (weights != null) {
      for (int i = 0; i < weights.size(); i++) {
        Common.setValueByName(soResult, "weight" + (i + 1), weights.get(i));
      }
    }
    soResultRepository.save(soResult);


    // save result details
    products.sort((a, b) -> Math.round((b.getScore() - a.getScore()) * 1000000000.0f));
    List<SOResultDetail> soResultDetails = new ArrayList<>();
    for (int i = 0; i < products.size(); i++) {
      SolrProduct product = products.get(i);
      SOResultDetail soResultDetail = new SOResultDetail();
      soResultDetail.setRank(i + 1);
      soResultDetail.setResultId(soResult.getId());
      soResultDetail.setScore(product.getScore());
      soResultDetail.setTitle(product.getTitle());
      soResultDetail.setUrl(product.getUrl());
      soResultDetails.add(soResultDetail);
    }
    soResultDetailRepository.save(soResultDetails);

    // evaluate
    /*
     * Calculate the score by comparing the result and the truth as follows.
     * Fetch the truth data specified by truth parameter.
     * Calculate the score by the following formula:
     * s = N - |Gm - Rn|
     * s = s < 0 ? 0 : s
     * score = SUM ( s^e(m) )
     * N	: Number of items in Google search result
     * Gm	: A rank of a page in Google search result [1 ≦ m ≦ N]
     * Rn	: A rank of a page in API search result [1 ≦ n ≦ N]
     * 	  Rn is 0 if a page is missing in API result.
     * t	: Threshold for priority ranks (e.g: 3)
     * e(x)	:  {2 if x ≦ t, 1 if x > t}
     */
    Map<String, SOResultDetail> urlResultsMap = soResultDetails.stream()
        .collect(Collectors.toMap(r -> r.getUrl().toLowerCase(), r -> r));
    List<SOTruthDetail> soTruthDetails = soTruthDetailRepository.findByTruthId(soTruth.getId());
    int N = soTruthDetails.size();
    int t = evaluateThreshold;
    float sum = 0;

    logger.info("start evaluate for product");
    for (SOTruthDetail soTruthDetail : soTruthDetails) {

      SOResultDetail resultDetail = urlResultsMap.get(soTruthDetail.getUrl().toLowerCase());

      if (resultDetail == null) {
        logger.info(String.format("[truth] %d:%s - [result] null - sum:%f", soTruthDetail.getRank(),
            soTruthDetail.getUrl(), sum));
        continue;
      } else {
        logger.info(String.format("[truth] %d:%s - [result] %d:%s - sum:%f", soTruthDetail.getRank(),
            soTruthDetail.getUrl(),
            resultDetail.getRank(), resultDetail.getUrl(), sum));
      }


      int e = soTruthDetail.getRank() <= t ? 2 : 1;
      sum += Math.pow((N - Math.abs(soTruthDetail.getRank() - resultDetail.getRank())), e);
    }

    SOEvaluation evaluation = new SOEvaluation();
    evaluation.setScore(sum);
    evaluation.setResultId(soResult.getId());
    evaluation.setTruthId(soTruth.getId());
    evaluation.setSiteId(soTruth.getSiteId());
    soEvaluationRepository.save(evaluation);

    logger.info("evaluation-id: " + evaluation.getId() + " score = " + sum);

    return evaluation;
  }
}
