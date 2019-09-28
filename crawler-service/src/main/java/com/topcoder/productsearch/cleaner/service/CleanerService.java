package com.topcoder.productsearch.cleaner.service;

import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.models.PageSearchCriteria;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.converter.service.SolrService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;

/**
 * the cleaner service
 */
@Service
@Setter
public class CleanerService {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(CleanerService.class);


  /**
   * the page repository
   */
  @Autowired
  PageRepository pageRepository;

  /**
   * WebSite entity access
   */
  @Autowired
  WebSiteRepository webSiteRepository;

  /**
   * the solr service
   */
  @Autowired
  SolrService solrService;

  /**
   * Data Clean Up Process:
   *
   * @param webSiteId the website id, it can be null
   * @throws InterruptedException when thread interrupted
   */
  public void clean(Integer webSiteId) throws InterruptedException {
    
    WebSite webSite = webSiteRepository.findOne(webSiteId);

    Common.readAndProcessPage(new PageSearchCriteria(webSiteId, null),
        webSite.getParallelSize(), pageRepository, (threadPoolExecutor, cPage) ->
        threadPoolExecutor.submit(() -> {
          logger.info("cleaner process for url " + cPage.getUrl());
          cleanPage(cPage,webSite.getPageExpiredPeriod());
        }));
  }

  /**
   * clean page
   *
   * @param cPage the page entity
   */
  public void cleanPage(CPage cPage, int pageExpiredPeriod) {


    /*
     Expired pages will be deleted from Solr.  If the page has not been updated since a specified time,
     Field last_modified_at in the pages table will be used to determine the time of last update.
     */
    Date expiresDate = java.sql.Date.valueOf(LocalDate.now().minusDays(pageExpiredPeriod));
    boolean needClean = false;
    if (cPage.getLastModifiedAt().before(expiresDate)) {
      needClean = true;
    }

    // if need clean or crawler marked as deleted
    if (needClean || cPage.getDeleted()) {
      logger.info("clean page for " + cPage.getUrl());
      try {
        solrService.deleteByURL(cPage.getUrl()); // remove from solr
      } catch (Exception e) {
        logger.error("delete from solr failed");
        e.printStackTrace();
      }
    }
  }
}
