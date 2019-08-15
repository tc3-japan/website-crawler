package com.topcoder.productsearch.converter;

import com.topcoder.productsearch.cleaner.service.CleanerService;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.converter.service.SolrService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;

/**
 * the convert thread
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConvertThread implements Runnable {

  /**
   * the page entity
   */
  CPage cPage;

  /**
   * solr service
   */
  SolrService solrService;

  /**
   * the page repository
   */
  PageRepository pageRepository;

  /**
   * the clean service
   */
  CleanerService cleanerService;

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(ConvertThread.class);


  @Override
  public void run() {

    try {
      /*
       * If the deleted field for a record in the pages table is set to true then the corresponding record,
       * as referenced by the same URL (pages.url -> manufacturer_product.product_url)
       * in the Solr Index will be deleted if it exists.
       */
      logger.info("converter process for url " + cPage.getUrl());
      if (cPage.getDeleted()) {
        solrService.deleteByURL(cPage.getUrl());
        logger.info("delete from solr url = " + cPage.getUrl());
      } else if (cPage.getLastProcessedAt() == null || cPage.getLastModifiedAt().after(cPage.getLastProcessedAt())) {

        // For each record Data clean up process will also be executed.  See details under “Data Clean Up Process”
        cleanerService.cleanPage(cPage);

        // create or update to avoid creating duplicate records
        solrService.createOrUpdate(cPage);
        logger.info("createOrUpdate to solr url = " + cPage.getUrl());

        // After processing this record the last_processed_at date-time will be time stamped.
        cPage.setLastProcessedAt(Date.from(Instant.now()));
        pageRepository.save(cPage);
      }
    } catch (Exception e) {
      logger.error("solr operation failed !");
      e.printStackTrace();
    }

  }
}
