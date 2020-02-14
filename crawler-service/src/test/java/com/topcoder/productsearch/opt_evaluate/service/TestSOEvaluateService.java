package com.topcoder.productsearch.opt_evaluate.service;

import com.topcoder.productsearch.api.models.ProductSearchRequest;
import com.topcoder.productsearch.api.models.SolrProduct;
import com.topcoder.productsearch.common.entity.*;
import com.topcoder.productsearch.common.repository.SOEvaluationRepository;
import com.topcoder.productsearch.common.repository.SOResultDetailRepository;
import com.topcoder.productsearch.common.repository.SOResultRepository;
import com.topcoder.productsearch.common.repository.SOTruthDetailRepository;
import com.topcoder.productsearch.converter.service.SolrService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

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

    WebSite webSite = new WebSite();
    webSite.setId(1);

    // test
    float eps = 0.000001f;

    SOEvaluation evaluation = soEvaluateService.evaluate(webSite, soTruth, "test word", weights);
    assertEquals(evaluation.getScore(), 65, eps);

    solrProducts.get(0).setScore(4.f);
    solrProducts.get(1).setScore(2.f);
    solrProducts.get(2).setScore(5.f);
    solrProducts.get(3).setScore(3.f);
    solrProducts.get(4).setScore(1.f);

    evaluation = soEvaluateService.evaluate(webSite, soTruth, "test word", weights);
    assertEquals(evaluation.getScore(), 37, eps);

    solrProducts.get(0).setUrl("url10");
    evaluation = soEvaluateService.evaluate(webSite, soTruth, "test word", weights);
    assertEquals(evaluation.getScore(), 34, eps);

    when(soTruth.getSearchWords()).thenReturn("test word");
    evaluation = soEvaluateService.evaluate(webSite, soTruth, null, null);
    assertEquals(evaluation.getScore(), 34, eps);
  }
}
