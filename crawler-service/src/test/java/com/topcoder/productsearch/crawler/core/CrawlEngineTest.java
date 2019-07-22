package com.topcoder.productsearch.crawler.core;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.topcoder.productsearch.common.entity.Page;
import com.topcoder.productsearch.crawler.BaseTest;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Unit tests for {@code CrawlEngine}.
 */
public class CrawlEngineTest extends BaseTest {

  /**
   * Mock crawler service.
   */
  @Mock
  private CrawlerService crawlerService;

  /**
   * Mock downloader
   */
  @Mock
  private Downloader downloader;

  /**
   * Mock scheduler
   */
  @Mock
  private Scheduler scheduler = new Scheduler(new Deduplicator());

  /**
   * Mock crawler
   */
  @Mock
  private Crawler crawler;

  /**
   * {@code CrawlEngine} under test.
   */
  private CrawlEngine crawlEngine;

  @Before
  public void setUp() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setDaemon(true);
    taskExecutor.initialize();

    crawlEngine = new CrawlEngine(new Settings(), downloader, scheduler, taskExecutor,
        crawlerService);
  }

  @Test
  public void testSave() {
    doNothing().when(crawlerService).save(any(Page.class), anyListOf(String.class));

    crawlEngine.save(new Page(), Arrays.asList("http://www.example.com", "http://www.uniqlo.com"));

    verify(crawlerService, times(1)).save(any(Page.class), anyListOf(String.class));
  }

  @Test
  public void testScheduleForDownload() {
    doReturn(true).when(scheduler).schedule(any(CrawlRequest.class));

    crawlEngine.scheduleForDownload(crawler, Arrays.asList("http://www.example.com",
        "http://www.uniqlo.com"), 1);

    verify(scheduler, times(2)).schedule(any(CrawlRequest.class));
  }

  @Test
  public void testScheduleForDownload_Skip_TooDeep_Pages() {
    crawlEngine.scheduleForDownload(crawler, Arrays.asList("http://www.example.com"), 100);
    verifyZeroInteractions(scheduler);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testRun() {
    AtomicInteger c = new AtomicInteger(0);
    when(crawler.getStartUrls()).thenReturn(Collections.singletonList("http://www.example.com"));
    doAnswer(invocation -> {
      if (c.getAndIncrement() == 0) {
        crawlEngine.scheduleForDownload(crawler, Collections.singletonList("http://uniqlo.com"), 1);
      }
      return null;
    }).when(crawler).process(any(CrawlRequest.class), eq(crawlEngine));
    when(downloader.submit(any(CrawlRequest.class), any(Consumer.class), any(Consumer.class)))
        .thenAnswer(invocation -> {
          CrawlRequest cr = invocation.getArgumentAt(0, CrawlRequest.class);
          invocation.getArgumentAt(1, Consumer.class).accept(cr);
          return true;
        });
    Deque<CrawlRequest> requests = new ArrayDeque<>();
    when(scheduler.schedule(any(CrawlRequest.class))).thenAnswer(invocation -> {
      requests.offer(invocation.getArgumentAt(0, CrawlRequest.class));
      return true;
    });
    when(scheduler.hasMoreRequests()).thenAnswer(invocation -> !requests.isEmpty());
    when(scheduler.getNextRequest()).thenAnswer(invocation -> requests.poll());

    crawlEngine.run(crawler);

    verify(crawler, times(1)).getStartUrls();
    verify(crawler, times(2)).process(any(CrawlRequest.class), eq(crawlEngine));
    verify(downloader, times(2))
        .submit(any(CrawlRequest.class), any(Consumer.class), any(Consumer.class));
  }
}