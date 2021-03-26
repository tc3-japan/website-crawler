package com.topcoder.productsearch.converter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.topcoder.productsearch.cleaner.service.CleanerService;
import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.models.PageSearchCriteria;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.converter.ConvertThread;

import lombok.Setter;

/**
 * the converter service
 */
@Service
@Setter
public class ConverterService {


  /**
   * the page repository
   */
  @Autowired
  private PageRepository pageRepository;

  /**
   * the solr service
   */
  @Autowired
  SolrService solrService;

  /**
   * the cleaner service
   */
  @Autowired
  CleanerService cleanerService;


  /**
   * convert page into solr
   *
   * @param webSiteId the website id
   * @throws InterruptedException when thread interrupted
   */
  public void convert(WebSite webSite) throws InterruptedException {
    if (webSite == null) {
      throw new IllegalArgumentException("webSite must be specified.");
    }
    Common.readAndProcessPage(
        new PageSearchCriteria(webSite.getId(), null), // specify site-id
        webSite.getParallelSize(), // batch-size TODO: need modification
        pageRepository,
        (threadPoolExecutor, cPage) ->
            threadPoolExecutor.submit(new ConvertThread(cPage, solrService, pageRepository, cleanerService,
             webSite.getPageExpiredPeriod().longValue())));
  }

  public void convert(WebSite webSite, String url) {
    if (webSite == null) {
      throw new IllegalArgumentException("webSite must be specified.");
    }
    CPage cPage = pageRepository.findByUrl(url);
    new ConvertThread(cPage, solrService, pageRepository, cleanerService, webSite.getPageExpiredPeriod().longValue()).run();
  }
}
