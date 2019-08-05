package com.topcoder.productsearch.converter;

/**
 * Exception during conversion from database to Solr.
 */
public class ConverterException extends Exception {

  /**
   * Constructor that takes a message and a cause.
   * @param message the message
   * @param cause the cause
   */
  public ConverterException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor that takes a message argument.
   * @param message the message
   */
  public ConverterException(String message) {
    super(message);
  }
}
