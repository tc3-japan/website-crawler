package com.topcoder.productsearch.crawler.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@code Deduplicator}
 */
public class DeduplicatorTest {

  /**
   * Test the hasSeen method.
   */
  @Test
  public void testHasSeen() {
    Deduplicator deduplicator = new Deduplicator();
    Assert.assertFalse(deduplicator.hasSeen("http://www.example.com"));
    Assert.assertTrue(deduplicator.hasSeen("http://www.example.com"));
    Assert.assertFalse(deduplicator.hasSeen("http://www.google.com"));
  }
}