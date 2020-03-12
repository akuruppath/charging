package com.ajai.chargingsession.handlers;

import java.util.Collections;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 
 * Central class that is responsible for handling all the exceptions thrown from the application and
 * returning a custom response back to the client.
 * 
 * @author ajai
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Provides custom handling of {@link java.lang.IllegalStateException}.
   * 
   * @param ex Instance of IllegalStateException
   * @return ResponseEntity with the captured exception message.
   */
  @ExceptionHandler(IllegalStateException.class)
  public final ResponseEntity<ApiError> handleIllegalStateException(Exception ex) {
    return new ResponseEntity<>(new ApiError(Collections.singletonList(ex.getMessage())),
        new HttpHeaders(), HttpStatus.NOT_FOUND);
  }

  /**
   * Provides custom handling of {@link java.lang.IllegalArgumentException}, 
   * {@link org.springframework.web.bind.MethodArgumentNotValidException}, 
   * {@link java.lang.NumberFormatException}.
   * 
   * @param ex Instance of an Exception 
   * @return ResponseEntity with the captured exception message.
   */
  @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class, NumberFormatException.class})
  public final ResponseEntity<ApiError> handleInvalidArgumentsException(Exception ex) {
    return new ResponseEntity<>(new ApiError(Collections.singletonList(ex.getMessage())),
        new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }
}

