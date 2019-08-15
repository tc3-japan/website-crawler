package com.topcoder.productsearch.common.util;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



import static org.junit.Assert.assertEquals;


/**
 * unit test for spring tool
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringToolTest {


  @Test
  public void testApplicationContext() {
    SpringTool springTool = new SpringTool();
    springTool.setApplicationContext(null);
    assertEquals(SpringTool.getApplicationContext(), null);
  }

}