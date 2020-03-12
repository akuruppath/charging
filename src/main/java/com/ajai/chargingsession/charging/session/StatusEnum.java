package com.ajai.chargingsession.charging.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * Represents the possible statuses of a charging session.
 * 
 * @author ajai
 *
 */
@ApiModel(description = "Possible statuses of a charging session.")
public enum StatusEnum {

  IN_PROGRESS("IN_PROGRESS"), FINISHED("FINISHED");

  @ApiModelProperty(notes = "Status of a charging session.")
  private String status;

  private StatusEnum(String status) {
    this.status = status;
  }

  /**
   * Gets the status
   * 
   * @return status on this enum.
   */
  public String getStatus() {
    return status;
  }
}
