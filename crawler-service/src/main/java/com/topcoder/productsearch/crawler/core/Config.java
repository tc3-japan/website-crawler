package com.topcoder.productsearch.crawler.core;

import com.topcoder.productsearch.common.repository.DestinationUrlRepository;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.crawler.service.CrawlerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration class for Crawler.
 */
@Configuration
public class Config {

  /**
   * {@code Settings} bean that contains crawler settings.
   */
  @Bean
  public Settings crawlerSettings() {
    return new Settings();
  }

  /**
   * {@code Deduplicator } bean.
   *
   * @return a {@code Deduplicator} object
   */
  @Bean
  public Deduplicator deduplicator() {
    return new Deduplicator();
  }

  /**
   * {@code Scheduler} bean.
   *
   * @param deduplicator - a {@code Deduplicator} bean
   * @return - a {@code Scheduler} object.
   */
  @Bean
  public Scheduler scheduler(Deduplicator deduplicator) {
    return new Scheduler(deduplicator);
  }

  /**
   * {@code Downloader} bean.
   *
   * @param taskScheduler - a {@code TaskScheduler} bean.
   * @param restTemplate - a {@code RestTemplate} bean.
   * @param settings - settings object
   * @return - a {@code Downloader} bean.
   */
  @Bean
  public Downloader downloader(
      @Qualifier("downloadTaskExecutor") ThreadPoolTaskExecutor taskScheduler,
      RestTemplate restTemplate, Settings settings) {
    return new Downloader(taskScheduler, restTemplate, settings.getRequestInterval(),
        settings.getDownloadQueueSize());
  }

  /**
   * {@code TaskScheduler} bean.
   *
   * @return - a {@code TaskScheduler} bean.
   */
  @Bean
  public ThreadPoolTaskExecutor downloadTaskExecutor(Settings settings) {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(settings.getDownloadPoolSize());
    return taskExecutor;
  }

  /**
   * {@code RestTemplate} bean.
   *
   * @param restTemplateBuilder - Rest template builder object.
   * @return - a {@code RestTemplate} bean.
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder,
      Settings settings) {
    return restTemplateBuilder
        .setConnectTimeout((int) settings.getPageDownloadTimeout().toMillis())
        .setReadTimeout((int) settings.getPageDownloadTimeout().toMillis())
        .build();
  }

  /**
   * {@code TaskExecutor} for processing the downloaded pages.
   *
   * @param settings - crawler settings
   */
  @Bean
  public TaskExecutor bodyProcessingTaskExecutor(Settings settings) {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(settings.getResponseProcessPoolSize());
    taskExecutor.setDaemon(true);
    return taskExecutor;
  }

  /**
   * {@code CrawlerEngine} instance.
   *
   * @param settings - settings of the crawler
   * @param downloader - downloader
   * @param scheduler - scheduler
   * @param taskExecutor - thread pool for processing the downloaded page.
   * @param crawlerService - service for querying and saving entities.
   * @return {@code CrawlerEngine} object
   */
  @Bean
  public CrawlEngine crawlerEngine(Settings settings, Downloader downloader,
      Scheduler scheduler, @Qualifier("bodyProcessingTaskExecutor") TaskExecutor taskExecutor,
      CrawlerService crawlerService) {
    return new CrawlEngine(settings, downloader, scheduler, taskExecutor, crawlerService);
  }

  @Bean
  public CrawlerService crawlerService(DestinationUrlRepository destinationUrlRepository,
      PageRepository pageRepository) {
    return new CrawlerService(destinationUrlRepository, pageRepository);
  }
}
