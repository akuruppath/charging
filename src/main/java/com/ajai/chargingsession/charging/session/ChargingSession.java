package com.ajai.chargingsession.charging.session;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a charging session.
 * 
 * @author ajai
 *
 */
@ApiModel(description = "All details about a charging session.")
public class ChargingSession {

  @ApiModelProperty(notes = "Generated charging session id.")
  @NotBlank
  private final UUID id;

  @ApiModelProperty(notes = "Station id")
  @NotBlank
  private String stationId;

  @ApiModelProperty(notes = "Initiation date-time of a charging session")
  @NotBlank
  private final LocalDateTime startedAt;

  @ApiModelProperty(notes = "Termination date-time of a charging session")
  @JsonInclude(Include.NON_NULL)
  private LocalDateTime stoppedAt;

  @ApiModelProperty(notes = "Status of a charging session")
  @NotBlank
  private StatusEnum status;

  private ChargingSession(UUID id, String stationId, LocalDateTime startedAt,
      LocalDateTime stoppedAt, StatusEnum status) {
    this.id = id;
    this.stationId = stationId;
    this.startedAt = startedAt;
    this.stoppedAt = stoppedAt;
    this.status = status;
  }

  /**
   * Represents a builder to create a ChargingSession.
   * 
   * @author ajai
   *
   */
  public static class ChargingSessionBuilder {

    @NotBlank
    public UUID id;

    @NotBlank
    public String stationId;

    @NotBlank
    public LocalDateTime startedAt;

    public LocalDateTime stoppedAt;

    @NotBlank
    public StatusEnum status;

    /**
     * A builder function that populates the fields for a Charging Session prior to building it.
     * 
     * @param builderFunction Consumer that consumes a passed in builder function.
     * 
     * @return ChargingSessionBuilder instance of ChargingSessionBuilder
     */
    public ChargingSessionBuilder with(Consumer<ChargingSessionBuilder> builderFunction) {
      builderFunction.accept(this);
      return this;
    }

    /**
     * Build the ChargingSession based on the parameters.
     * 
     * @return newly created instance of ChargingSession.
     */
    public ChargingSession build() {
      return new ChargingSession(id, stationId, startedAt, stoppedAt, status);
    }

  }

  /**
   * 
   * Get the id of this object.
   *
   * @return current id
   */
  public UUID getId() {
    return id;
  }

  /**
   * Get the station id of this object.
   * 
   * @return current stationId
   */
  public String getStationId() {
    return stationId;
  }

  /**
   * Sets the stationId on this object.
   * 
   * @param stationId id of the charging station.
   */
  public void setStationId(String stationId) {
    this.stationId = stationId;
  }

  /**
   * Gets the date and time of start of a charging session.
   * 
   * @return current start datetime
   */
  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  /**
   * Gets the date and time of stop of a charging session.
   * 
   * @return current start datetime
   */
  public LocalDateTime getStoppedAt() {
    return stoppedAt;
  }

  /**
   * Sets the date and time of stop of a charging session.
   * 
   * @param stoppedAt date time of stoppage
   */
  public void setStoppedAt(LocalDateTime stoppedAt) {
    this.stoppedAt = stoppedAt;
  }

  /**
   * Gets the charging status of this object.
   * 
   * @return current status
   */
  public StatusEnum getStatus() {
    return status;
  }

  /**
   * 
   * Sets the charging status of this object.
   * 
   * @param status charging status.
   */
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

}
