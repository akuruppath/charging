package com.ajai.chargingsession.charging.session;

import java.util.Map;

import org.springframework.util.Assert;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a charging session summary.
 * <p>
 * Every object of this type receives the required information in form of a {@link Map}.
 * </p>
 * 
 * @author ajai
 *
 */
@ApiModel(description = "Summary of charging sessions.")
public class ChargingSessionSummary {

  @ApiModelProperty(notes = "The total number of charging sessions")
  private final long totalCount;

  @ApiModelProperty(notes = "The number of charging sessions initiated.")
  private final long startedCount;

  @ApiModelProperty(notes = "The number of charging sessions terminated.")
  private final long stoppedCount;

  /**
   * Creates a new instance of a ChargingSessionSummary.
   * 
   * @param summaryMap map with charging session summary information
   */
  public ChargingSessionSummary(Map<StatusEnum, Long> summaryMap) {
    Assert.notNull(summaryMap, "Expected a valid summary map.");
    this.startedCount =
        summaryMap.get(StatusEnum.IN_PROGRESS) == null ? 0 : summaryMap.get(StatusEnum.IN_PROGRESS);
    this.stoppedCount =
        summaryMap.get(StatusEnum.FINISHED) == null ? 0 : summaryMap.get(StatusEnum.FINISHED);
    this.totalCount = startedCount + stoppedCount;
  }

  /**
   * Get totalCount on this object.
   * 
   * @return current totalCount
   */
  public long getTotalCount() {
    return totalCount;
  }

  /**
   * Get startedCount on this object
   * 
   * @return current startedCount
   */
  public long getStartedCount() {
    return startedCount;
  }

  /**
   * Get stoppedCount on this object
   * 
   * @return current stoppedCount
   */
  public long getStoppedCount() {
    return stoppedCount;
  }

  public String toString() {
    return new StringBuilder().append("totalCount : ").append(totalCount).append(" this.startedCount : ")
        .append(this.startedCount).append(" this.stoppedCount : ").append(this.stoppedCount)
        .toString();
  }

}
