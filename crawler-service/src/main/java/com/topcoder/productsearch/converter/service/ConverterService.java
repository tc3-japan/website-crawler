package com.topcoder.productsearch.converter.service;

import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.common.repository.PageRepository;
import com.topcoder.productsearch.common.repository.WebSiteRepository;
import com.topcoder.productsearch.converter.ConverterException;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import javax.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * The converter service that converts records in the MySQL database into indexed documents in
 * Apache Solr.
 */
@Service
@Slf4j
@ConfigurationProperties("converter-settings")
public class ConverterService {

  /**
   * The solr collection name
   */
  private static final String SOLR_COLLECTION_NAME = "manufacturer_product";

  /**
   * The "product" page type.
   */
  private static final String PAGE_TYPE_PRODUCT = "product";

  /**
   * The WebSite repository.
   */
  private WebSiteRepository webSiteRepository;

  /**
   * The CPage repository.
   */
  private PageRepository pageRepository;

  /**
   * The SolrJ client for create/update/delete documents.
   */
  private SolrClient solrClient;

  /**
   * The process/cleanup batch size.
   */
  @Setter
  private int batchSize;

  /**
   * Time lapse period for Expired pages in number of Days.
   */
  @Setter
  private int timeLapsePeriodInDays;

  /**
   * Number of pages expired.
   */
  @Getter
  private int pagesExpired = 0;
  /**
   * Number of pages deleted from Solr.
   */
  @Getter
  private int pagesDeleted = 0;
  /**
   * Number of pages created in Solr.
   */
  @Getter
  private int pagesAdded = 0;
  /**
   * Number of pages updated in Solr.
   */
  @Getter
  private int pagesUpdated = 0;

  @Autowired
  public ConverterService(WebSiteRepository webSiteRepository,
      PageRepository pageRepository, SolrClient solrClient) {
    this.webSiteRepository = webSiteRepository;
    this.pageRepository = pageRepository;
    this.solrClient = solrClient;
  }

  /**
   * Run the conversion and/or clean process on one or all of the websites.
   *
   * @param siteId      the id of the website to convert, convert all websites if null.
   * @param cleanUpOnly run only clean process if true.
   */
  @Transactional
  public void process(Integer siteId, boolean cleanUpOnly) throws ConverterException {
    logger.info("Start converter on {} with --only-data-cleanup={}",
        siteId != null ? siteId : "ALL-WEB-SITES", cleanUpOnly);

    Iterable<WebSite> webSites;
    if (siteId == null) {
      webSites = webSiteRepository.findAll();
    } else {
      WebSite webSite = webSiteRepository.findOne(siteId);
      if (webSite == null) {
        throw new ConverterException("Could not find website for id=" + siteId);
      }
      webSites = Collections.singletonList(webSite);
    }

    for (WebSite webSite : webSites) {
      logger.info("Processing web site id={}, name={}", webSite.getId(), webSite.getName());
      resetCounters();
      try {
        cleanUp(webSite);
        if (!cleanUpOnly) {
          convert(webSite);
        }
        webSite.setLastModifiedAt(new Date());
        webSiteRepository.save(webSite);

        logger.info("WebSite (id={}, name={}) stats: "
                + "pages expired={}, "
                + "pages deleted={}, "
                + "pages added={}, "
                + "pages updated={}.",
            webSite.getId(), webSite.getName(),
            pagesExpired,
            pagesDeleted,
            pagesAdded,
            pagesUpdated);
      } catch (RuntimeException e) {
        logger.error("Failed to run conversion on website id={}, name={}.", webSite.getId(),
            webSite.getName(), e);
        throw new ConverterException("Failed to run conversion on website " + webSite, e);
      }
    }

    try {
      solrClient.close();
    } catch (IOException e) {
      logger.error("Error while closing SolrClient.", e);
    }
  }

  /**
   * Run the clean-up process on the given website.
   *
   * @param webSite the website to clean up.
   */
  private void cleanUp(WebSite webSite) throws ConverterException {
    logger.info("Running cleanup on website (id={} name={})", webSite.getId(), webSite.getName());
    // Mark expired not-deleted pages as deleted, and update their last_modified_at field to now.
    Instant expiredPageDeletedAt = Instant.now();
    Date expiryCutoffTime = Date
        .from(expiredPageDeletedAt.minus(timeLapsePeriodInDays, ChronoUnit.DAYS));
    int expired = pageRepository
        .markExpiredWebPagesAsDeleted(webSite.getId(), PAGE_TYPE_PRODUCT,
            Date.from(expiredPageDeletedAt), expiryCutoffTime);
    logger.info("{} expired pages marked as deleted.", expired);
    pagesExpired += expired;

    // Deletes any pages that are marked deleted since last clean up.
    try {
      Date lastCleanUpAt = webSite.getLastCleanedUpAt();
      batchProcessPages(webSite, lastCleanUpAt, true, (batch) -> {
        List<String> documentsToDelete = new ArrayList<>();
        for (CPage cPage : batch) {
          documentsToDelete.add(cPage.getUrl());
          cPage.setLastProcessedAt(null);
          cPage.setLastProcessedAt(new Date());
        }
        try {
          logger.info("{} pages to delete.", documentsToDelete.size());
          UpdateResponse response = solrClient
              .deleteById(SOLR_COLLECTION_NAME, documentsToDelete);
          logger.info("SolrServer response to deleteById request: {}", response.getResponse());
          pagesDeleted += documentsToDelete.size();
        } catch (SolrServerException | IOException e) {
          throw new RuntimeException("Failed to delete documents from Solr.", e);
        }

        pageRepository.save(batch);
      });
      solrClient.commit(SOLR_COLLECTION_NAME);

      webSite.setLastCleanedUpAt(new Date());
      logger.info("Update WebSite (id={}, name={}) last clean up time to {}", webSite.getId(),
          webSite.getName(), webSite.getLastCleanedUpAt());
    } catch (IOException | SolrServerException e) {
      logger.error("Failed to clean up", e);
      throw new ConverterException("Failed to clean up deleted pages from Solr.", e);
    }
  }

  /**
   * Run the conversion process on the given website.
   *
   * @param webSite the website to convert.
   **/
  private void convert(WebSite webSite) throws ConverterException {
    try {
      logger.info("Running convert on website (id={} name={})", webSite.getId(), webSite.getName());
      batchProcessPages(webSite, webSite.getLastProcessedAt(), false, (batch) -> {
        List<SolrInputDocument> documentsToSync = new ArrayList<>();

        for (CPage page : batch) {
          SolrInputDocument manufacturerProduct = new SolrInputDocument();
          manufacturerProduct.setField("id", page.getUrl());
          manufacturerProduct.setField("product_url", page.getUrl());
          manufacturerProduct.setField("manufacturer_name", webSite.getName());
          manufacturerProduct.setField("page_updated_at", new Date());
          manufacturerProduct.setField("html_title", page.getTitle());
          manufacturerProduct.setField("html_body", page.getBody());
          documentsToSync.add(manufacturerProduct);
          if (page.getLastProcessedAt() == null) {
            pagesAdded++;
          } else {
            pagesUpdated++;
          }
          page.setLastProcessedAt(new Date());
          page.setLastModifiedAt(new Date());
        }

        try {
          logger.info("{} pages to add/update.", documentsToSync.size());
          UpdateResponse addResponse = solrClient.add(SOLR_COLLECTION_NAME, documentsToSync);
          logger.info("SolrServer response for solrClient.add: {}", addResponse.getResponse());
        } catch (IOException | SolrServerException e) {
          throw new RuntimeException("Failed to add/update documents to Solr.", e);
        }

        pageRepository.save(batch);
      });
      solrClient.commit(SOLR_COLLECTION_NAME);

      webSite.setLastProcessedAt(new Date());
      logger.info("Update WebSite (id={}, name={}) last processed time to {}", webSite.getId(),
          webSite.getName(), webSite.getLastProcessedAt());
    } catch (IOException | SolrServerException e) {
      throw new ConverterException("Failed to get SolrClient.", e);
    }
  }

  /**
   * Generic batch-process method that fetch batches of CPage from database and pass them to the
   * provided consumer.
   *
   * @param webSite                the website object to which the pages belong
   * @param modificationCutoffTime the last processed/cleaned up timestamp, can be null
   * @param deleted                return the deleted or not-deleted pages
   * @param consumer               the consumer of batches of web pages
   */
  private void batchProcessPages(WebSite webSite, Date modificationCutoffTime,
      boolean deleted, Consumer<Page<CPage>> consumer) {
    Pageable pageable = new PageRequest(0, batchSize);
    Page<CPage> batch;
    do {
      if (modificationCutoffTime == null) {
        batch = pageRepository.findAllWebPages(webSite.getId(), PAGE_TYPE_PRODUCT, deleted,
            pageable);
      } else {
        batch = pageRepository.findModifiedWebPages(webSite.getId(), PAGE_TYPE_PRODUCT,
            modificationCutoffTime, deleted, pageable);
      }
      if (batch.getNumberOfElements() > 0) {
        consumer.accept(batch);
      }
      pageable = pageable.next();
    } while (!batch.isLast());
  }

  private void resetCounters() {
    pagesExpired = 0;
    pagesAdded = 0;
    pagesUpdated = 0;
    pagesDeleted = 0;
  }
}
