package com.topcoder.productsearch.common;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.topcoder.productsearch.cleaner.service.CleanerService;
import com.topcoder.productsearch.cleaner.service.ValidatePagesService;
import com.topcoder.productsearch.common.entity.SOTruth;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.SOTruthRepository;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.converter.service.ConverterService;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import com.topcoder.productsearch.crawler.service.CrawlerServiceCreator;
import com.topcoder.productsearch.opt_evaluate.service.SOEvaluateService;
import com.topcoder.productsearch.opt_gen_truth.service.SOGenTruthService;

/**
 * unit test for CrawlerRunner
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class ProcessLauncherTest {


  @Mock
  WebSiteRepository webSiteRepository;

  @Mock
  ConverterService converterService;

  @Mock
  CrawlerService crawlerService;

  @Mock
  CrawlerServiceCreator crawlerServiceCreator;

  @Mock
  CleanerService cleanerService;

  @Mock
  ValidatePagesService validatePagesService;

  @Mock
  SOTruthRepository soTruthRepository;

  @Mock
  SOEvaluateService soEvaluateService;

  @Mock
  SOGenTruthService soGenTruthService;

  @Mock
  SOTruth soTruth;

  @InjectMocks
  ProcessLauncher processLauncher;

  @Test
  public void testRunner() throws Exception {
    // input
    int siteId = 1;

    DefaultApplicationArguments appArgs = new DefaultApplicationArguments(
        new String[]{
            "--site=" + siteId,
            "--proc=crawler"
        }
    );

    WebSite webSite = new WebSite();
    webSite.setId(siteId);

    when(webSiteRepository.findOne(siteId)).thenReturn(webSite);
    when(webSiteRepository.findOne(Integer.valueOf(1))).thenReturn(webSite);
    when(webSiteRepository.findOne(2)).thenReturn(null);
    when(soTruthRepository.findOne(1)).thenReturn(soTruth);
    when(soTruthRepository.findOne(2)).thenReturn(null);
    doNothing().when(crawlerService).crawler();
    doNothing().when(converterService).convert(webSite);
    doNothing().when(converterService).convert(webSite);
    doNothing().when(converterService).convert(null);
    doNothing().when(cleanerService).clean(webSite);
    // doNothing().when(cleanerService).clean(null);
    doNothing().when(validatePagesService).validate(webSite);


    try {
      processLauncher.run(appArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }

    verify(crawlerServiceCreator, times(1)).getCrawlerService(any(Integer.class));

    DefaultApplicationArguments args = new DefaultApplicationArguments(new String[]{"--proc=crawler"});
    try {
      processLauncher.run(args);
    } catch (Exception e) {
      //assertEquals("Missing parameter '--site=<site-id>'", e.getMessage());
    }

    DefaultApplicationArguments args2 = new DefaultApplicationArguments(new String[]{"--proc=cleaner", "--site=1"});
    try {
      processLauncher.run(args2);
    } catch (Exception e) {
      e.printStackTrace();
    }

    args2 = new DefaultApplicationArguments(new String[]{"--proc=cleaner"});
    try {
      processLauncher.run(args2);
    } catch (Exception e) {
      e.printStackTrace();
    }

    DefaultApplicationArguments args3 = new DefaultApplicationArguments(new String[]{"--proc=crawler", "--site=2"});
    try {
      processLauncher.run(args3);
    } catch (Exception e) {
      assertEquals("Could not create CrawlerService where website id = 2", e.getMessage());
    }

    DefaultApplicationArguments args4 = new DefaultApplicationArguments(new String[]
        {"--proc=converter", "--site=2"});
    try {
      processLauncher.run(args4);
    } catch (Exception e) {
      assertEquals("can not find website where id = 2", e.getMessage());
    }

    DefaultApplicationArguments args5 = new DefaultApplicationArguments(new String[]{});
    processLauncher.run(args5);

    args5 = new DefaultApplicationArguments(new String[]{"--proc=converter", "--only-data-cleanup"});
    processLauncher.run(args5);
    // verify(cleanerService, times(1)).clean(null);

    args5 = new DefaultApplicationArguments(new String[]{"--proc=converter"});
    processLauncher.run(args5);
    verify(converterService, times(0)).convert(webSite);


    args5 = new DefaultApplicationArguments(new String[]{"--proc=validate-pages", "--site=1"});
    processLauncher.run(args5);
    verify(validatePagesService, times(1)).validate(webSite);


    args = new DefaultApplicationArguments(new String[]{"--proc=opt_evaluate", "--site=1", "--weights=1.1,2.5,3,4",
        "--truth=1", "--search-words=\"クルーネックT MEN\""});
    processLauncher.run(args);
    verify(soEvaluateService, times(1)).evaluate(any(SOTruth.class),anyString(),anyList());

    args = new DefaultApplicationArguments(new String[]{"--proc=opt_evaluate", "--site=1", "--weights=1.1,2.5,3,4", "--search-words=\"クルーネックT MEN\""});
    try {
      processLauncher.run(args);
    } catch (Exception e) {
      assertEquals(e.getMessage(), "parameter truth is required");
    }
    verify(soEvaluateService, times(1)).evaluate( any(SOTruth.class), anyString(), anyList());

    args = new DefaultApplicationArguments(new String[]{"--proc=opt_evaluate", "--site=1", "--weights=1.1,2.5,3,x",
        "--truth=1"});
    processLauncher.run(args);
    verify(soEvaluateService, times(1)).evaluate(any(SOTruth.class),anyString(),anyList());

    args = new DefaultApplicationArguments(new String[]{"--proc=opt_evaluate", "--site=1", "--weights=1.1,2.5,3,x",
        "--truth=2"});
    try {
      processLauncher.run(args);
    } catch (Exception e) {
      assertEquals(e.getMessage(), "cannot find truth where id = 2");
    }
    verify(soEvaluateService, times(1)).evaluate( any(SOTruth.class), anyString(), anyList());

    args = new DefaultApplicationArguments(new String[]{"--proc=opt_evaluate", "--site=1", "--truth=1"});
    processLauncher.run(args);
    verify(soEvaluateService, times(2)).evaluate( any(SOTruth.class), anyString(), anyList());

    args = new DefaultApplicationArguments(new String[]{"--proc=opt_gen_truth", "--site=1"});
    processLauncher.run(args);
    verify(soGenTruthService, times(1)).genTruth(any(WebSite.class), anyString(), anyString());

    args = new DefaultApplicationArguments(new String[]{"--proc=opt_gen_truth", "--site=1","--search-words=\"test word\""});
    processLauncher.run(args);
    verify(soGenTruthService, times(2)).genTruth(any(WebSite.class), anyString(), anyString());

    args = new DefaultApplicationArguments(new String[]{"--proc=opt_gen_truth", "--site=1","--search-words-path=\"test path\""});
    processLauncher.run(args);
    verify(soGenTruthService, times(3)).genTruth(any(WebSite.class), anyString(), anyString());
  }


}