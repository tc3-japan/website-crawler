package com.topcoder.productsearch.common;


import com.topcoder.productsearch.cleaner.service.CleanerService;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.converter.service.ConverterService;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
  CleanerService cleanerService;

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
    when(webSiteRepository.findOne(2)).thenReturn(null);
    doNothing().when(crawlerService).crawler(webSite);
    doNothing().when(converterService).convert(1);
    doNothing().when(converterService).convert(2);
    doNothing().when(converterService).convert(null);
    doNothing().when(cleanerService).clean(any(Integer.class));
    doNothing().when(cleanerService).clean(null);


    try {
      processLauncher.run(appArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    verify(webSiteRepository, times(1)).findOne(any(Integer.class));


    DefaultApplicationArguments args = new DefaultApplicationArguments(new String[]{"--proc=crawler"});
    try {
      processLauncher.run(args);
    } catch (Exception e) {
      assertEquals("Missing parameter '--site=<site-id>'", e.getMessage());
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
      assertEquals("can not find website where id = 2", e.getMessage());
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
    verify(cleanerService, times(1)).clean(null);

  }


}