package com.topcoder.productsearch.api.exceptions;

/**
 * Not Found exception which will get converted to 404 error by spring boot
 * 
 */
public class NotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NotFoundException(String message) {
    super(message);
  }
}
