package com.topcoder.productsearch.common.util;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


/**
 * find the real task from future task
 * https://www.javaspecialists.eu/archive/Issue228.html
 */
public class JobDiscoverer {
  private final static Field callableInFutureTask;
  private static final Class<? extends Callable> adapterClass;
  private static final Field runnableInAdapter;

  static {
    try {
      callableInFutureTask =
          FutureTask.class.getDeclaredField("callable");
      callableInFutureTask.setAccessible(true);
      adapterClass = Executors.callable(() -> {
      }).getClass();
      runnableInAdapter = adapterClass.getDeclaredField("task");
      runnableInAdapter.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * get the real task from future task
   *
   * @param task the future task
   * @return the real task
   */
  public static Object findRealTask(Runnable task) {
    try {
      Object callable = callableInFutureTask.get(task);
      if (adapterClass.isInstance(callable)) {
        return runnableInAdapter.get(callable);
      } else {
        return callable;
      }
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
}
