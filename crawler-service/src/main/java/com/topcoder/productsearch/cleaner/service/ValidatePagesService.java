package com.topcoder.productsearch.cleaner.service;

import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.converter.service.SolrService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * the validate pages service
 */
@Service
@Setter
public class ValidatePagesService {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(ValidatePagesService.class);

  /**
   * the page expired period time, unit is day
   */
  @Value("${crawler-settings.page-expired-period}")
  private Long pageExpiredPeriod;

  /**
   * the page repository
   */
  @Autowired
  PageRepository pageRepository;

  /**
   * the solr service
   */
  @Autowired
  SolrService solrService;

  /**
   * parallel run/page size
   */
  @Value("${crawler-settings.parallel-size}")
  private int parallelSize;

  /**
   * Data Clean Up Process:
   *
   * @param webSiteId the website id, it can be null
   * @throws InterruptedException when thread interrupted
   */
  public void validate(Integer webSiteId) throws InterruptedException {
    Common.readAndProcessPage(webSiteId, parallelSize, pageRepository, (threadPoolExecutor, cPage) ->
        threadPoolExecutor.submit(() -> {
          this.process(cPage);
        }));
  }

  /**
   * process page
   *
   * @param cPage the db page
   */
  public void process(CPage cPage) {
    logger.info("validate url " + cPage.getUrl());
    if (Common.isUrlBroken(cPage.getUrl())) {
      cPage.setDeleted(true);
      pageRepository.save(cPage);
      logger.info("mark " + cPage.getUrl() + " deleted=true");
    }
  }
}
