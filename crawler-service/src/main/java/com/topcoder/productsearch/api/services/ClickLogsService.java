package com.topcoder.productsearch.api.services;

import java.time.Instant;
import java.util.Date;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.RequiredArgsConstructor;

import com.topcoder.productsearch.api.exceptions.BadRequestException;
import com.topcoder.productsearch.common.entity.ClickLogs;
import com.topcoder.productsearch.common.repository.ClickLogsRepository;
import com.topcoder.productsearch.common.util.Common;

/**
 * the click logs service
 */
@Service
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ClickLogsService {

  /**
   * the click logs repository
   */
  @Autowired
  private ClickLogsRepository clickLogsRepository;

  /**
   * create new click logs
   *
   * @param searchId the request value of search_id
   * @param searchWords the request value of search_words
   * @param pageUrl the request value of page_url
   * @param pageRank the request value of page_rank
   * @return response message
   */
  public String create(String searchId, String searchWords, String pageUrl, Integer pageRank) {
    try {
      ClickLogs clickLogs = new ClickLogs();
      clickLogs.setSearchId(searchId);
      clickLogs.setSearchWords(searchWords);
      clickLogs.setNormalizedSearchWords(getNormalizedSearchWords(searchWords));
      clickLogs.setPageUrl(pageUrl);
      clickLogs.setPageRank(pageRank);
      clickLogs.setCreatedDate(Date.from(Instant.now()));
      clickLogs.setCreatedAt(Date.from(Instant.now()));
      clickLogs.setLastModifiedAt(null);
      clickLogsRepository.save(clickLogs);
      return "Successfully registered the click logs";
    } catch (DataIntegrityViolationException dException) {
      throw new BadRequestException(dException.getMostSpecificCause().getMessage());
    }
  }

  /**
   * get normalized search words
   *
   * @param searchWords the request value of search_words
   * @return normalized search words
   */
  private String getNormalizedSearchWords(String searchWords) {

    return Common.normalizeSearchWord(searchWords);
  }

}
