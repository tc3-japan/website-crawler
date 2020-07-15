package com.topcoder.productsearch.opt_evaluate.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
import com.topcoder.productsearch.converter.service.SolrService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit test for SOEvaluateService
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSOEvaluateService {

  @Mock
  SolrService solrService;

  @Mock
  SOResultRepository soResultRepository;

  @Mock
  SOResultDetailRepository soResultDetailRepository;

  @Mock
  SOTruthDetailRepository soTruthDetailRepository;

  @Mock
  SOEvaluationRepository soEvaluationRepository;

  @InjectMocks
  SOEvaluateService soEvaluateService;


  @Mock
  SOResult soResult;
  @Mock
  SOResultDetail soResultDetail;
  @Mock
  SOEvaluation soEvaluation;

  @Mock
  SOTruth soTruth;

  @Test
  public void testEvaluate() throws Exception {

    // mock
    when(soEvaluationRepository.save(any(SOEvaluation.class))).thenReturn(soEvaluation);
    when(soResultRepository.save(any(SOResult.class))).thenReturn(soResult);
    when(soResultDetailRepository.save(any(SOResultDetail.class))).thenReturn(soResultDetail);

    soEvaluateService.setListSize(3);
    soEvaluateService.setEvaluateThreshold(2);
    List<SOTruthDetail> truthDetails = new ArrayList<>();
    List<SolrProduct> solrProducts = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      SOTruthDetail soTruthDetail = new SOTruthDetail();
      soTruthDetail.setUrl("url" + i);
      soTruthDetail.setRank(i + 1);
      truthDetails.add(soTruthDetail);

      SolrProduct solrProduct = new SolrProduct();
      solrProduct.setScore((5 - i) * 1.0f);
      solrProduct.setUrl("url" + i);
      solrProducts.add(solrProduct);
    }

    when(solrService.searchProduct(any(ProductSearchRequest.class))).thenReturn(solrProducts);
    when(soTruthDetailRepository.findByTruthId(anyInt())).thenReturn(truthDetails);
    List<Float> weights = Arrays.asList(1.0f, 2.0f);

    // test
    float eps = 0.000001f;

    SOEvaluation evaluation = soEvaluateService.evaluate(soTruth, "test word", weights);
    assertEquals(1.285714, evaluation.getScore(), eps);

    solrProducts.get(0).setScore(4.f);
    solrProducts.get(1).setScore(2.f);
    solrProducts.get(2).setScore(5.f);
    solrProducts.get(3).setScore(3.f);
    solrProducts.get(4).setScore(1.f);

    evaluation = soEvaluateService.evaluate(soTruth, "test word", weights);
    assertEquals(0.523809, evaluation.getScore(), eps);

    solrProducts.get(0).setUrl("url10");
    evaluation = soEvaluateService.evaluate(soTruth, "test word", weights);
    assertEquals(0.476190, evaluation.getScore(), eps);

    when(soTruth.getSearchWords()).thenReturn("test word");
    evaluation = soEvaluateService.evaluate(soTruth, null, null);
    assertEquals(0.476190, evaluation.getScore(), eps);
  }

  @Test
  public void testCalculateScore_IdealRank() throws Exception {
    int listSize = 5;
    soEvaluateService.setListSize(listSize);
    soEvaluateService.setEvaluateThreshold(2);

    Map<String, Integer> truthRanking = new HashMap<String, Integer>();
    for (int i=0; i<listSize; i++) {
      truthRanking.put("URL_" + i, i+1);
    }
    Map<String, Integer> actualRanking = new HashMap<String, Integer>();
    actualRanking.putAll(truthRanking);
    int score = (int)soEvaluateService.calculateScore(actualRanking, truthRanking);

    int expectedScore = (int) Math.pow(5, 2) + (int) Math.pow(5, 2) + 5 + 5 + 5;
    assertEquals(expectedScore, score);

    List<SOTruthDetail> truthDetails = createTruthDetails(truthRanking);
    int idealScore = (int) soEvaluateService.calculateIdealScore(truthDetails);

    assertEquals(expectedScore, idealScore);


    List<SOResultDetail> actualDetails = createResultDetails(actualRanking);
    int nScore = (int) soEvaluateService.calculateNormalizedScore(actualDetails, truthDetails);

    assertEquals(1, nScore);
  }

  @Test
  public void testCalculateScore() throws Exception {
    int listSize = 5;
    soEvaluateService.setListSize(listSize);
    soEvaluateService.setEvaluateThreshold(2);

    int[] truth = new int[] {1, 2, 3, 4, 5};
    int[] actual = new int[] {2, 3, 1, 5, 4};
    int expectedScore = (int) Math.pow((5-1), 2) + (int) Math.pow((5-1), 2) + (5-(3-1)) + (5-(5-4)) + (5-(5-4));

    testCalculateScore(expectedScore, actual, truth, soEvaluateService);
  }

  @Test
  public void testCalculateScore2() throws Exception {
    int listSize = 5;
    soEvaluateService.setListSize(listSize);
    soEvaluateService.setEvaluateThreshold(2);

    int[] truth = new int[] {1,2,3};
    int[] actual = new int[] {2, 3};
    int expectedScore = (int) Math.pow((5-1), 2) + (int) Math.pow((5-1), 2) + 0 + 0 + 0;

    testCalculateScore(expectedScore, actual, truth, soEvaluateService);
  }

  public void testCalculateScore(int expectedScore, int[] actual, int[] truth, SOEvaluateService evaluateService) throws Exception {

    Map<String, Integer> truthRanking = new HashMap<String, Integer>();
    for (int i=0; i<truth.length; i++) {
      truthRanking.put("URL_" + i, truth[i]);
    }
    Map<String, Integer> actualRanking = new HashMap<String, Integer>();
    for(int i=0; i<actual.length; i++) {
      actualRanking.put("URL_" + i, actual[i]);
    }

    int score = (int)soEvaluateService.calculateScore(actualRanking, truthRanking);
    assertEquals(expectedScore, score);

    List<SOTruthDetail> truthDetails = createTruthDetails(truthRanking);
    float idealScore = soEvaluateService.calculateIdealScore(truthDetails);
    float expectedNScore = score / idealScore;

    List<SOResultDetail> actualDetails = createResultDetails(actualRanking);
    float nScore = soEvaluateService.calculateNormalizedScore(actualDetails, truthDetails);

    assertEquals(expectedNScore, nScore, 0.0000001F);
  }

  @SuppressFBWarnings(value={"WMI_WRONG_MAP_ITERATOR"}, justification="no need to care it")
  List<SOTruthDetail> createTruthDetails(Map<String, Integer> ranking) {
    List<SOTruthDetail> truths = new LinkedList<SOTruthDetail>();

    Map<Integer, String> sortedMap = new TreeMap<Integer, String>();
    for (Entry<String, Integer> e : ranking.entrySet()) {
      sortedMap.put(e.getValue(), e.getKey());
    }

    for (Integer rank : sortedMap.keySet()) {
      SOTruthDetail detail = new SOTruthDetail();
      detail.setRank(rank);
      detail.setUrl(sortedMap.get(rank));
      truths.add(detail);
    }
    return truths;
  }

  @SuppressFBWarnings(value={"WMI_WRONG_MAP_ITERATOR"}, justification="no need to care it")
  List<SOResultDetail> createResultDetails(Map<String, Integer> ranking) {
    List<SOResultDetail> results = new LinkedList<SOResultDetail>();

    Map<Integer, String> sortedMap = new TreeMap<Integer, String>();
    for (Entry<String, Integer> e : ranking.entrySet()) {
      sortedMap.put(e.getValue(), e.getKey());
    }

    for (Integer rank : sortedMap.keySet()) {
      SOResultDetail detail = new SOResultDetail();
      detail.setRank(rank);
      detail.setUrl(sortedMap.get(rank));
      results.add(detail);
    }
    return results;
  }
}
