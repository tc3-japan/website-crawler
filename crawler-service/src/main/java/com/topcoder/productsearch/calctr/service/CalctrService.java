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
   * the p value
   */
  @Value("${calctr.p}")
  @Setter
  Float p;

  /**
   * the t value
   */
  @Value("${calctr.t}")
  @Setter
  Float t;

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
   * last n days
   */
  private int lastNDays;

  /**
   * start calctr processtor
   *
   * @param lastNDays the last N days
   */
  public void calctr(int lastNDays) {
    this.lastNDays = lastNDays;
    // step 1
    List<ClickLogCount1> count1s = countBySearchWords();
    // step 2
    List<ClickLogCount2> count2s = countBySearchWordsAndUrl();
    // step 3
    this.calculateCTR(count2s, count1s);

    // step 4
    List<SolrDocument> solrDocuments;
    List<String> proceeds = new ArrayList<>();
    try {
      solrDocuments = solrService.findByURLs(count2s.stream().map(ClickLogCount2::getUrl).collect(Collectors.toList()));
      logger.info(String.format("step4: found solrDocuments by urls, size = %d", solrDocuments.size()));
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("calctr get documents by url failed, " + e.getMessage());
      return;
    }

    for (ClickLogCount2 c2 : count2s) {
      SolrInputDocument document;
      try {
        document = getSolrDocument(solrDocuments, c2);
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
        proceeds.add(document.get("id").getValue().toString());
      } catch (Exception e) {
        e.printStackTrace();
        logger.error(e.getMessage());
      }
    }

    // step 5, Update all documents which have CTR except for documents updated in (4) as:
    if (proceeds.isEmpty()) {
      logger.info("no any document proceed, so we skip step 5");
      return;
    }

    try {
      solrDocuments = solrService.findByCtrAndIds(proceeds);
      logger.info(String.format("step5: found solrDocuments by urls and exclude ids, size = %d", solrDocuments.size()));
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("step5: calctr get documents by url and exclude id failed, " + e.getMessage());
      return;
    }

    for (SolrDocument document : solrDocuments) {
      try {
        // New CTR = (ctr * P  < T) ? (ctr * P) : 0
        double ctr = Double.parseDouble(document.get(CTR).toString());
        document.setField(CTR, ctr * p < t ? ctr * p : 0);
        solrService.createOrUpdate(document);
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
      // this is new document, so need remove all information and add new ID
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
      if (cachedC1s.containsKey(c2.getWords())) {
        ClickLogCount1 c1 = cachedC1s.get(c2.getWords());
        c2.setCtr(c2.getCnt() * 1.0f / c1.getCnt());
      } else {
        c2.setCtr(1);
      }
    });
  }

  /**
   * Count the number of records per search_words:
   *
   * @return the list of count
   */
  List<ClickLogCount1> countBySearchWords() {
    String sqlTpl = "select normalized_search_words, count(*) as cnt from click_logs where created_at > (CURRENT_DATE - %d) group by normalized_search_words";
    List<Object[]> db = entityManager.createNativeQuery(String.format(sqlTpl, this.lastNDays)).getResultList();
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
  List<ClickLogCount2> countBySearchWordsAndUrl() {
    String sqlTpl = "select normalized_search_words,page_url, count(*) as c2, max(created_date) as latest_click_date from click_logs where created_at > (CURRENT_DATE - %d) group by normalized_search_words, page_url";
    List<Object[]> db = entityManager.createNativeQuery(String.format(sqlTpl, this.lastNDays)).getResultList();
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

}
