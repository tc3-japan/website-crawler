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
import org.springframework.beans.factory.annotation.Value;


import java.time.Instant;
import java.time.LocalDate;
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
   * Expiry Period
   */
  Long pageExpiredPeriod;

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(ConvertThread.class);



  @Override
  public void run() {
    Date expireDate = java.sql.Date.valueOf(LocalDate.now().minusDays(pageExpiredPeriod));
    try {
      /*
       * If the deleted field for a record in the pages table is set to true then the corresponding record,
       * as referenced by the same URL (pages.url -> manufacturer_product.product_url)
       * in the Solr Index will be deleted if it exists.
       */
      logger.info("converter process for page#" + cPage.getId() + ", " + cPage.getUrl());
      if (cPage.getDeleted()) {
        solrService.deleteByURL(cPage.getUrl());
        logger.info("URL was marked deleted in database so deleting from solr index. page#" + cPage.getId() + ", " + cPage.getUrl());
      } else if (cPage.getLastModifiedAt() != null && (cPage.getLastModifiedAt().before(expireDate)))  {
        solrService.deleteByURL(cPage.getUrl());
        logger.info("URL has not been modified for a long time, deleting from solr index. page#"+ cPage.getId() + ", " + cPage.getUrl());
      } else if (cPage.getLastProcessedAt() == null || cPage.getLastModifiedAt().after(cPage.getLastProcessedAt())) {

        // create or update to avoid creating duplicate records
        solrService.createOrUpdate(cPage);
        logger.info("write to solr index. page#" + cPage.getId() + ", " + cPage.getUrl());

        // After processing this record the last_processed_at date-time will be time stamped.
        cPage.setLastProcessedAt(Date.from(Instant.now()));
        pageRepository.save(cPage);
      } else {
        logger.info("converter: page not updated. page#" + cPage.getId());
      }
    } catch (RuntimeException re ) {
      logger.error("RuntimeException during solr operation: "+ re.getMessage());
      re.printStackTrace();
    } catch (Exception e) {
      logger.error("solr operation failed !");
      e.printStackTrace();
    }

  }
}
