/*
 * Copyright (C) 2019 TopCoder Inc., All Rights Reserved.
 */
package com.topcoder.productsearch.api.exceptions.handlers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.topcoder.productsearch.api.exceptions.BadRequestException;
import com.topcoder.productsearch.api.exceptions.NotFoundException;
import com.topcoder.productsearch.api.exceptions.UnauthorizedException;

/**
 * The exception handler that maps exceptions to corresponding response status
 * and message.
 *
 * @author TCSCODER
 * @version 1.0
 */
@RestControllerAdvice
public class ServiceExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * The message source.
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * Build error response.
     *
     * @param status  the http status
     * @param message the message
     * @return the error response entity with code and message
     */
    private static ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", message);
        return new ResponseEntity<>(responseBody, status);
    }

    /**
     * Handle illegal argument exception.
     *
     * @param ex the exception
     * @return the error response entity
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }


    /**
     * Handle entity not found exception.
     *
     * @param ex the exception
     * @return the error response entity
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ResponseEntity<Object> handleEntityNotFoundException(NotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handle bad request exception.
     *
     * @param ex the exception
     * @return the error response entity
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

  /**
   * Handle unauthorized exception.
   *
   * @param ex the exception
   * @return the error response entity
   */
  @ExceptionHandler(UnauthorizedException.class)
  @ResponseBody
  public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  /**
   * Handle access denied exception.
   *
   * @param ex the exception
   * @return the error response entity
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseBody
  public ResponseEntity<Object> handleUnauthorizedException(AccessDeniedException ex) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
  }

    /**
     * Handle the other exceptions.
     *
     * @param throwable the throwable
     * @return the error response entity
     */
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<Object> handleOtherException(Throwable throwable) {
        throwable.printStackTrace();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + throwable.getMessage());
    }

    /**
     * A single place to customize the response body of all exception types.
     *
     * @param ex      the exception
     * @param body    the body for the response
     * @param headers the headers for the response
     * @param status  the response status
     * @param request the current request
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        String message = ex.getMessage();

        if (ex instanceof HttpMessageNotReadableException) {
            message = "Request body is missing or invalid";
            if (ex.getCause() != null && ex.getCause() instanceof JsonMappingException &&
                    ex.getCause().getCause() != null && ex.getCause().getCause() instanceof IllegalArgumentException) {
                message = ex.getCause().getCause().getMessage();
            }
        } else if (ex instanceof MethodArgumentNotValidException) {
            message = convertErrorsToMessage(((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors());
        } else if (ex instanceof BindException) {
            message = convertErrorsToMessage(((BindException) ex).getBindingResult().getAllErrors());
        } else if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            message = "Internal server error";
        }

        return buildErrorResponse(status, message);
    }

    /**
     * Convert object errors to error message string.
     *
     * @param objectErrors the list of object errors
     * @return the comma separated error message
     */
    private String convertErrorsToMessage(List<ObjectError> objectErrors) {
        List<String> messages = new ArrayList<>();

        for (ObjectError objectError : objectErrors) {
            messages.add(messageSource.getMessage(objectError, null));
        }

        return StringUtils.collectionToDelimitedString(messages, ", ");
    }
}
