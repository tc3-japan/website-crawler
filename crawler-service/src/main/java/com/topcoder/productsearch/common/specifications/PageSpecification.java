package com.topcoder.productsearch.common.specifications;

import com.topcoder.productsearch.common.entity.CPage;
import com.topcoder.productsearch.common.models.PageSearchCriteria;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;


/**
 * page search Specification
 */
@AllArgsConstructor
public class PageSpecification implements Specification<CPage> {

  /**
   * the page search Criteria
   */

  PageSearchCriteria pageSearchCriteria;

  /**
   * build Predicate
   *
   * @param root  the class root
   * @param query the db query
   * @param cb    the Criteria builder
   * @return Predicate
   */
  @Override
  public Predicate toPredicate(Root<CPage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    List<Predicate> predicates = new ArrayList<>();
    if (pageSearchCriteria.getDeleted() != null) {
      predicates.add(cb.equal(root.get("deleted"), pageSearchCriteria.getDeleted()));
    }

    if (pageSearchCriteria.getWebsiteId() != null) {
      predicates.add(cb.equal(root.get("siteId"), pageSearchCriteria.getWebsiteId()));
    }
    return query.where(cb.and(predicates.toArray(new Predicate[0]))).distinct(true).getRestriction();
  }
}
