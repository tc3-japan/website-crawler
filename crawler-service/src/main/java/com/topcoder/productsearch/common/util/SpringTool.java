package com.topcoder.productsearch.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * get spring context in non spring object
 */
@Component
public class SpringTool implements ApplicationContextAware {

  /**
   * the spring application context
   */
  private static ApplicationContext context = null;
  private static final Logger logger = LoggerFactory.getLogger(SpringTool.class);

  /**
   * inject application context
   *
   * @param applicationContext the application context
   * @throws BeansException if error happened in injection
   */
  @Override
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    logger.info("inject spring context ...");

    SpringTool.context = applicationContext;
  }

  /**
   * get injected context
   *
   * @return the application context
   */
  public static ApplicationContext getApplicationContext() {
    return context;
  }
} 