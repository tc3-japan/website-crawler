package com.topcoder.productsearch.converter.service;

import com.topcoder.productsearch.cleaner.service.CleanerService;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.models.PageSearchCriteria;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.common.util.Common;
import com.topcoder.productsearch.converter.ConvertThread;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
   * WebSite repository
   */
  private WebSiteRepository WebSiteRepository;

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
  public void convert(Integer webSiteId) throws InterruptedException {
    WebSite webSite = WebSiteRepository.findOne(webSiteId);
    Common.readAndProcessPage(new PageSearchCriteria(webSiteId, null),
        webSite.getParallelSize(), pageRepository, (threadPoolExecutor, cPage) ->
            threadPoolExecutor.submit(new ConvertThread(cPage, solrService, pageRepository, cleanerService,
             webSite.getPageExpiredPeriod().longValue())));
  }
}
