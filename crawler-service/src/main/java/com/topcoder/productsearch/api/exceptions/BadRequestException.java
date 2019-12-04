package com.topcoder.productsearch.api.exceptions;

/**
 * Bad request exception which will get converted to 400 error by spring boot
 */
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super(message);
    }
}
