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
   * the page repository
   */
  @Autowired
  PageRepository pageRepository;


  /**
   * WebSite repository 
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
  public void validate(WebSite webSite) throws InterruptedException {

    // WebSite webSite = webSiteRepository.findOne(webSiteId);
    
    Common.readAndProcessPage(new PageSearchCriteria(webSite.getId(), false),
        webSite.getParallelSize(), pageRepository, (threadPoolExecutor, cPage) ->
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
