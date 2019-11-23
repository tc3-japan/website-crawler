package com.topcoder.productsearch.common.util;

import org.apache.commons.codec.digest.DigestUtils;

public class HashGenerator {

  public String hash(String str) {
    if (str == null) {
      throw new IllegalArgumentException("str is required.");
    }
    return DigestUtils.sha256Hex(str);
  }
}
