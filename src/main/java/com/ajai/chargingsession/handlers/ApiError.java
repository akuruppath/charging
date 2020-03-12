package com.ajai.chargingsession.handlers;

import java.util.List;

/**
 * 
 * Class that encapsulates all the error messages.
 * 
 * @author ajai
 *
 */
public class ApiError {

  private final List<String> errors;

  /**
   * Creates an instance of ApiError with a list of errors.
   * 
   * @param errors List of errors to set.
   */
  public ApiError(List<String> errors) {
    this.errors = errors;
  }

  /**
   * Retrieves the list of errors of this instance
   * 
   * @return current list of errors on this object.
   */
  public List<String> getErrors() {
    return errors;
  }

}
