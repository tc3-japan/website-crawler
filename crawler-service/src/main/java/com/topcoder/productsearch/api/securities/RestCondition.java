package com.topcoder.productsearch.api.securities;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


/**
 * None rest mode processor don't need some bean, like SecurityConfig
 * this class used to determine if spring need inject that bean.
 */
public class RestCondition implements Condition {

  public static boolean isRest = false;

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return isRest;
  }
}