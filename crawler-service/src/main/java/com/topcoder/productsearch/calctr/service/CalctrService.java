package com.topcoder.productsearch.calctr.service;


import com.topcoder.productsearch.calctr.model.ClickLogCount1;
import com.topcoder.productsearch.calctr.model.ClickLogCount2;
import com.topcoder.productsearch.converter.service.SolrService;
import lombok.Setter;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * calctr process entry
 */
@Service
public class CalctrService {

  /**
   * the logger instance
   */
  private static final Logger logger = LoggerFactory.getLogger(CalctrService.class);

  /**
   * fields name
   */
  private static final String CTR_TERM = "ctr_term";
  private static final String CTR = "ctr";
  private static final String LAST_CLICKED_AT = "last_clicked_at";
  private static final String PRODUCT_URL = "product_url";
  public static final String VERSION = "_version_";
  public static final String ID = "id";


  /**
   * the decayRate
   */
  @Value("${calctr.ctr_decay_rate}")
  @Setter
  Float decayRate;

  /**
   * the minThreshold
   */
  @Value("${calctr.ctr_min_threshold}")
  @Setter
  Float minThreshold;

  /**
   * DB entity manager
   */
  @Autowired
  EntityManager entityManager;

  /**
   * solr service
   */
  @Autowired
  SolrService solrService;

  /**
   * start calctr processtor
   *
   * @param lastNDays the last N days
   */
  public void process(int lastNDays) {
    // step 1
    List<ClickLogCount1> count1s = countBySearchWords(lastNDays);
    // step 2
    List<ClickLogCount2> count2s = countBySearchWordsAndUrl(lastNDays);
    // step 3
    this.calculateCTR(count2s, count1s);

    // step 4
    List<SolrDocument> solrDocuments;
    List<String> proceeds = new ArrayList<>();
    try {
      solrDocuments = solrService.findByURLs(count2s.stream().map(ClickLogCount2::getUrl).collect(Collectors.toList()));
      logger.info(String.format("step4: found solrDocuments by urls, size = %d", solrDocuments.size()));
    } catch (Exception e) {
      logger.error("calctr get documents by url failed, " + e.getMessage(), e);
      return;
    }

    for (ClickLogCount2 c2 : count2s) {
      try {
        SolrInputDocument document = getSolrDocument(solrDocuments, c2);
        if (document == null) {
          logger.info("no document find in solr, where url = " + c2.getUrl());
          continue;
        }

        document.setField(CTR_TERM, c2.getWords());
        if (document.get(CTR) != null) {
          // (old CTR + new CTR) * 0.5
          document.setField(CTR, (Double.parseDouble(document.get(CTR).getFirstValue().toString()) + c2.getCtr()) * 0.5);
        } else {
          // (new CTR) * 0.5
          document.setField(CTR, c2.getCtr() * 0.5);
        }
        document.setField(LAST_CLICKED_AT, c2.getLastClickDate());
        solrService.createOrUpdate(document);
        proceeds.add(document.get(ID).getValue().toString());
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }

    // step 5, Update all documents which have CTR except for documents updated in (4) as:
    try {
      solrDocuments = solrService.findByCtrAndIds(proceeds);
      logger.info(String.format("step5: found solrDocuments by urls and exclude ids, size = %d", solrDocuments.size()));
    } catch (Exception e) {
      logger.error("step5: calctr get documents by url and exclude id failed, " + e.getMessage(), e);
      return;
    }

    for (SolrDocument document : solrDocuments) {
      try {
        // New CTR = (ctr * P)
        // delete the document if the new CTR is less than T.
        double ctr = Double.parseDouble(document.get(CTR).toString());
        double newCtr = ctr * decayRate;
        if (newCtr < minThreshold) {
          solrService.delete(document.get(ID).toString());
        } else {
          document.setField(CTR, newCtr);
          solrService.createOrUpdate(document);
        }
      } catch (Exception e) {
        logger.info("set new ctr failed");
        e.printStackTrace();
      }
    }

  }

  /**
   * get solr documents
   *
   * @param solrDocuments the solr documents
   * @return the exist or new solr document
   */
  SolrInputDocument getSolrDocument(List<SolrDocument> solrDocuments, ClickLogCount2 c2) throws IOException, SolrServerException {
    SolrDocument docWithoutCtr = null;

    // search by product url and words
    for (SolrDocument document : solrDocuments) {
      String url = document.get(PRODUCT_URL).toString();
      Object ctrTerm = document.get(CTR_TERM);
      if (c2.getUrl().equalsIgnoreCase(url)) {
        // use this to set new CTR_TERM
        if (ctrTerm == null) {
          docWithoutCtr = document;
        } else if (ctrTerm.toString().equalsIgnoreCase(c2.getWords())) {
          return solrService.createSolrInputDocument(document);
        }
      }
    }

    if (docWithoutCtr != null) {
      // make a copy of docWithoutCtr.
      // this is new document, so need remove version and add new ID
      SolrInputDocument document = solrService.createSolrInputDocument(docWithoutCtr);
      document.remove(VERSION);
      document.setField(ID, UUID.randomUUID().toString());
      return document;
    }
    return null;
  }


  /**
   * For each record in the result of (2), calculate CTR for page_url as:
   * CTR =  c2 / c1*
   * *c1 is obtained from a record in the result of (1), which has the same normalized_search_words.
   *
   * @param count2s the c2 list
   * @param count1s the c1 list
   */
  void calculateCTR(List<ClickLogCount2> count2s, List<ClickLogCount1> count1s) {
    Map<String, ClickLogCount1> cachedC1s = new HashMap<>();
    count1s.forEach(c1 -> {
      cachedC1s.put(c1.getWords(), c1);
    });
    count2s.forEach(c2 -> {
      if (c2.getWords() != null && cachedC1s.containsKey(c2.getWords())) {
        ClickLogCount1 c1 = cachedC1s.get(c2.getWords());
        c2.setCtr(c2.getCnt() * 1.0f / c1.getCnt());
      } else {
        logger.warn(String.format("Unexpected situation. No C1 is found for '%s'", c2.getWords()));
      }
    });
  }

  /**
   * Count the number of records per search_words:
   *
   * @return the list of count
   */
  @SuppressWarnings("unchecked")
  List<ClickLogCount1> countBySearchWords(int lastNDays) {
    String sqlTpl = "select normalized_search_words, count(*) as cnt from click_logs where created_at > (CURRENT_DATE - %d) group by normalized_search_words";
    List<Object[]> db = entityManager.createNativeQuery(String.format(sqlTpl, lastNDays)).getResultList();
    List<ClickLogCount1> logCount1List = new ArrayList<>();
    db.forEach(objects -> {
      ClickLogCount1 cnt = new ClickLogCount1();
      cnt.setWords(objects[0].toString());
      cnt.setCnt(Integer.valueOf(objects[1].toString()));
      logCount1List.add(cnt);
    });
    return logCount1List;
  }

  /**
   * Count the number of records per search_words and page_url with the latest click_date in aggregated records.
   */
  @SuppressWarnings("unchecked")
  List<ClickLogCount2> countBySearchWordsAndUrl(int lastNDays) {
    String sqlTpl = "select normalized_search_words,page_url, count(*) as c2, max(created_date) as latest_click_date from click_logs where created_at > (CURRENT_DATE - %d) group by normalized_search_words, page_url";
    List<Object[]> db = entityManager.createNativeQuery(String.format(sqlTpl, lastNDays)).getResultList();
    List<ClickLogCount2> count2List = new ArrayList<>();
    db.forEach(objects -> {
      ClickLogCount2 cnt = new ClickLogCount2();
      cnt.setWords(objects[0].toString());
      cnt.setUrl(objects[1].toString());
      cnt.setCnt(Integer.valueOf(objects[2].toString()));
      cnt.setLastClickDate(objects[3].toString());
      count2List.add(cnt);
    });
    return count2List;
  }

  /* for test */
  public void updateCTR(String docId, Float ctr, String term) throws Exception {
    SolrDocument doc = this.solrService.findDocumentById(docId);
    if (ctr == null || term == null) {
      doc.remove(CTR);
      doc.remove(CTR_TERM);
    } else {
      doc.setField(CTR, ctr);
      doc.setField(CTR_TERM, term);
    }
    this.solrService.createOrUpdate(doc);
  }
}
