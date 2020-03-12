package com.ajai.chargingsession.charging.dto;

import javax.validation.constraints.NotBlank;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a DTO that carries information related to the charging station where the charging was
 * initiated.
 * 
 * @author ajai
 *
 */
@ApiModel(description = "Detail of a charging station.")
public final class ChargingStationDTO {

  @ApiModelProperty(notes = "Id of a charging station.")
  @NotBlank
  private String stationId;

  @SuppressWarnings("unused")
  private ChargingStationDTO() {
    super();
  }

  /**
   * Creates a new instance of a ChargingStationDTO with a given stationId.
   * 
   * @param stationId id of the charging station
   */
  public ChargingStationDTO(String stationId) {
    this.stationId = stationId;
  }

  /**
   * Get the stationId of the current object.
   * 
   * @return current stationId.
   */
  public String getStationId() {
    return stationId;
  }


}
