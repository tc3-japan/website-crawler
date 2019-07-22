package com.topcoder.productsearch.crawler.service;

import com.topcoder.productsearch.common.entity.DestinationUrl;
import com.topcoder.productsearch.common.entity.Page;
import com.topcoder.productsearch.common.repository.DestinationUrlRepository;
import com.topcoder.productsearch.common.repository.PageRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service for saving and querying entities in one transaction.
 */
@Slf4j
public class CrawlerService {

  /**
   * The destination url repository bean.
   */
  private final DestinationUrlRepository destinationUrlRepository;

  /**
   * The page repository bean.
   */
  private final PageRepository pageRepository;

  /**
   * Constructor that takes a JPA repository.
   *
   * @param destinationUrlRepository - the {@code DestinationUrlRepository} bean.
   */
  @Autowired
  public CrawlerService(
      DestinationUrlRepository destinationUrlRepository,
      PageRepository pageRepository) {
    this.destinationUrlRepository = destinationUrlRepository;
    this.pageRepository = pageRepository;
  }

  /**
   * Save a collection of urls for a given page to database, ignoring those already exists.
   *
   * @param page - the source page entity
   * @param urls - the urls
   */
  @Transactional
  public void save(Page page, Collection<String> urls) {
    pageRepository.save(page);

    Set<String> existingUrls = destinationUrlRepository.findBySourcePage(page)
        .stream().map(DestinationUrl::getUrl)
        .collect(Collectors.toSet());

    List<DestinationUrl> newDestinationUrls = new ArrayList<>();
    for (String url : urls) {
      if (!existingUrls.contains(url)) {
        newDestinationUrls.add(new DestinationUrl(url, page));
      } else {
        logger.debug("Not saving existing url {}", url);
      }
    }

    destinationUrlRepository.save(newDestinationUrls);
  }

  /**
   * Find a page with the given url in database.
   *
   * @param url - the page url
   * @return the page or null if not found.
   */
  @Transactional
  public Page findByUrl(String url) {
    return pageRepository.findDistinctByUrl(url);
  }
}
