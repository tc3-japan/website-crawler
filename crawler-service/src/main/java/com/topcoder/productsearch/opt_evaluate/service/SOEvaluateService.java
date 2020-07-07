package com.topcoder.productsearch.opt_evaluate.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.stat.StatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.topcoder.productsearch.api.models.EvaluateRequest;
import com.topcoder.productsearch.api.models.EvaluationResult;
import com.topcoder.productsearch.api.models.ProductSearchRequest;
import com.topcoder.productsearch.api.models.SolrProduct;
import com.topcoder.productsearch.common.entity.SOEvaluation;
import com.topcoder.productsearch.common.entity.SOResult;
import com.topcoder.productsearch.common.entity.SOResultDetail;
import com.topcoder.productsearch.common.entity.SOTruth;
import com.topcoder.productsearch.common.entity.SOTruthDetail;
import com.topcoder.productsearch.common.repository.SOEvaluationRepository;
import com.topcoder.productsearch.common.repository.SOResultDetailRepository;
import com.topcoder.productsearch.common.repository.SOResultRepository;
import com.topcoder.productsearch.common.repository.SOTruthDetailRepository;
import com.topcoder.productsearch.common.repository.SOTruthRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.converter.service.SolrService;

import lombok.Setter;

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
   * search opt truth repository
   */
  @Autowired
  SOTruthRepository soTruthRepository;

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


  @Transactional
  public SOEvaluation evaluate(SOTruth soTruth, String searchWords, List<Float> weights, String queryType, boolean saveResult) throws Exception {

    if (soTruth == null || soTruth.getId() == null) {
      throw new IllegalArgumentException("Truth should be specified.");
    }
    // truth details
    List<SOTruthDetail> soTruthDetails = soTruthDetailRepository.findByTruthId(soTruth.getId());

    if (soTruthDetails == null || soTruthDetails.isEmpty()) {
      throw new IllegalArgumentException(String.format("Truth#%d has no detailed data.", soTruth.getId()));
    }


    // search w/ API
    String words = searchWords == null ? soTruth.getSearchWords() : searchWords;

    ProductSearchRequest request = new ProductSearchRequest();
    request.setManufacturerIds(Collections.singletonList(soTruth.getSiteId()));
    request.setWeights(weights);
    request.setQuery(Arrays.asList(words.split("\\s+")));
    request.setRows(listSize * 2);
    request.setParser(queryType);

    List<SolrProduct> products = solrService.searchProduct(request);
    List<SOResultDetail> soResultDetails = createSearchResultDetails(products);

    // evaluate
    //float score = calculateScore(soResultDetails, soTruthDetails);
    float score = calculateNormalizedScore(soResultDetails, soTruthDetails);

    // If saveResult is false, return the result here.
    if (!saveResult) {
      SOEvaluation evaluation = new SOEvaluation();
      evaluation.setScore(score);
      evaluation.setTruthId(soTruth.getId());
      evaluation.setSiteId(soTruth.getSiteId());
      return evaluation;
    }

    // Saving results into database
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
    soResultDetails.forEach(soResultDetail  ->  { soResultDetail.setResultId(soResult.getId()); });
    soResultDetailRepository.save(soResultDetails);

    SOEvaluation evaluation = new SOEvaluation();
    evaluation.setScore(score);
    evaluation.setResultId(soResult.getId());
    evaluation.setTruthId(soTruth.getId());
    evaluation.setSiteId(soTruth.getSiteId());
    soEvaluationRepository.save(evaluation);

    logger.info("evaluation-id: " + evaluation.getId() + " score = " + score);

    return evaluation;
  }

  /**
   * create reference data by scraping the result of Google search with some search words for the specific web site.
   *
   * @param soTruth     the search opt truth
   * @param searchWords the search words
   * @param weights     the search weights
   */
  public SOEvaluation evaluate(SOTruth soTruth, String searchWords, List<Float> weights) throws Exception {
    // queryType: standard, saveResult: true
    return evaluate(soTruth, searchWords, weights, null, true);
  }

  /**
   * Do the evaluation based on the request specified by EvaluateRequest.
   * @param evaluateRequest
   * @return
   * @throws Exception
   */
  public EvaluationResult evaluate(EvaluateRequest evaluateRequest) throws Exception {
    logger.info("starting the evaluation for " + evaluateRequest);
    if (evaluateRequest == null) {
      throw new IllegalArgumentException("evaluateRequest must be specified.");
    }
    long start = System.currentTimeMillis();

    List<Float> weights = evaluateRequest.getWeights();
    int truthId = evaluateRequest.getStartTruthId();
    int size = evaluateRequest.getSize() == null ? 1 : evaluateRequest.getSize();

    if (weights == null || weights.isEmpty()) {
      throw new IllegalArgumentException("weights must be specified.");
    }

    EvaluationResult result = new EvaluationResult();
    SOTruth headTruth = soTruthRepository.findOne(truthId);
    if (headTruth == null) {
      throw new IllegalArgumentException(String.format("the specified truth is invalid. #%d does not exist.", truthId));
    }
    List<SOTruth> soTruths = soTruthRepository.findFrom(headTruth.getSiteId(), truthId, new PageRequest(0, size));

    logger.info("# of tests: " + soTruths.size());
    List<Float> scores = Collections.synchronizedList(new ArrayList<Float>(soTruths.size()));
    AtomicInteger errorCount = new AtomicInteger(0);

    ScheduledExecutorService executer = Executors.newScheduledThreadPool(soTruths.size());
    //ScheduledExecutorService executer = Executors.newSingleThreadScheduledExecutor();
    for(SOTruth truth : soTruths) {
      executer.schedule(() -> {
        try {
          SOEvaluation eval = evaluate(truth, null, weights, evaluateRequest.getQueryType(), evaluateRequest.isSaveResult());
          scores.add(eval.getScore());
        } catch (Exception e) {
          logger.error(String.format("An error occured in evaluating w/ the truth#%d : %s", truth.getId(), e.getMessage()), e);
          errorCount.incrementAndGet();
        }
      }, 100, TimeUnit.MILLISECONDS);
    }
    executer.shutdown();
    if (!executer.awaitTermination(300, TimeUnit.SECONDS)) {
      executer.shutdownNow();
    }

    double[] dscores = scores.stream().mapToDouble(s -> s.doubleValue()).toArray();

    result.setWeights(weights);
    result.setDataCount(scores.size());
    result.setScoreMean((float) StatUtils.mean(dscores));
    result.setScoreMax((float) StatUtils.max(dscores));
    result.setScoreMin((float) StatUtils.min(dscores));
    result.setScoreMedian((float) StatUtils.percentile(dscores, 50));
    result.setScoreVariance((float) StatUtils.populationVariance(dscores));
    result.setErrorCount(errorCount.get());
    result.setElapsedTimeSeconds(elapsedTimeSeconds(start, System.currentTimeMillis()));

    logger.info("finished the evaluation for " + evaluateRequest);

    return result;
  }

  int elapsedTimeSeconds(long start, long end) {
    return (int) ((end - start) / 1000L);
  }

  /**
   * create SOResultDetail objects from the search result.
   * @param products
   * @return
   */
  private List<SOResultDetail> createSearchResultDetails(List<SolrProduct> products) {
    products.sort((a, b) -> Math.round((b.getScore() - a.getScore()) * 1000000000.0f));
    List<SOResultDetail> soResultDetails = new ArrayList<>();
    Set<String> urls = new HashSet<String>();
    for (int i = 0; i < products.size(); i++) {
      SolrProduct product = products.get(i);

      // skip product with URL duplication
      if (urls.contains(product.getUrl())) {
        continue;
      }
      urls.add(product.getUrl());
      SOResultDetail soResultDetail = new SOResultDetail();
      soResultDetail.setRank(i + 1);
      soResultDetail.setScore(product.getScore());
      soResultDetail.setTitle(product.getTitle());
      soResultDetail.setUrl(product.getUrl());
      soResultDetails.add(soResultDetail);
    }
    return soResultDetails;
  }

  protected float calculateNormalizedScore(List<SOResultDetail> soResultDetails, List<SOTruthDetail> soTruthDetails) {
    float score = calculateScore(soResultDetails, soTruthDetails);
    float iscore = calculateIdealScore(soTruthDetails);
    float nscore = iscore != 0 ? (score / iscore) : 0;

    logger.info(String.format("Truth#%d size:%d, Result size:%d, score:%f = (%f / %f)", soTruthDetails.get(0).getId(), soTruthDetails.size(), soResultDetails.size(), nscore, score, iscore));

    return nscore;
  }

  protected float calculateScore(List<SOResultDetail> soResultDetails, List<SOTruthDetail> soTruthDetails) {

    Map<String, Integer> actualRankingMap = new HashMap<String, Integer>();
    for(SOResultDetail d : soResultDetails) {
      actualRankingMap.put(d.getUrl().toString(), d.getRank());
    }
    Map<String, Integer> truthRankingMap = new HashMap<String, Integer>();
    for(SOTruthDetail d : soTruthDetails) {
      truthRankingMap.put(d.getUrl().toString(), d.getRank());
    }

    return calculateScore(actualRankingMap, truthRankingMap);
  }

  protected float calculateIdealScore(List<SOTruthDetail> soTruthDetails) {

    Map<String, Integer> truthRankingMap = new HashMap<String, Integer>();
    for(SOTruthDetail d : soTruthDetails) {
      truthRankingMap.put(d.getUrl().toString(), d.getRank());
    }

    return calculateScore(truthRankingMap, truthRankingMap);
  }

  /*
   * Calculate the score by comparing the result and the truth as follows.
   * Fetch the truth data specified by truth parameter.
   * Calculate the score by the following formula:
   * s = N - |Gm - Rn|
   * s = s < 0 ? 0 : s
   * score = SUM ( s^e(m) )
   * N  : Number of items in Google search result
   * Gm : A rank of a page in Google search result [1 ≦ m ≦ N]
   * Rn : A rank of a page in API search result [1 ≦ n ≦ N]
   *    Rn is 0 if a page is missing in API result.
   * t  : Threshold for priority ranks (e.g: 3)
   * e(x) :  {2 if x ≦ t, 1 if x > t}
   */
  protected float calculateScore(Map<String, Integer> actualRanking, Map<String, Integer> truthRanking) {
    int N = this.listSize;
    int t = evaluateThreshold;
    float sum = 0;

    for (Entry<String, Integer> truth : truthRanking.entrySet()) {

      String url = truth.getKey();
      Integer truthRank = truthRanking.get(url);
      Integer actualRank = actualRanking.get(url);

      if (actualRank == null) {
        logger.debug(String.format("[truth] %d - [result] n/a - +0 sum:%f, %s", truthRank, sum, url));
        continue;
      }

      int e = truthRank <= t ? 2 : 1;
      int s = N - Math.abs(truthRank - actualRank);
      s = s < 0 ? 0 : s;
      double score = Math.pow(s, e);
      sum += score;

      logger.debug(String.format("[truth] %d - [result] %d - +%f sum:%f, %s", truthRank, actualRank, score, sum, url));
    }
    return sum;
  }
}
