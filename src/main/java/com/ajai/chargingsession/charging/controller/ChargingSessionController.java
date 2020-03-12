package com.ajai.chargingsession.charging.controller;

import static com.ajai.chargingsession.constants.Constants.CHARGING_SESSION_ID;
import static com.ajai.chargingsession.constants.Constants.SECONDS;
import static com.ajai.chargingsession.constants.UrlConstants.URL_CHARGING_SESSION;
import static com.ajai.chargingsession.constants.UrlConstants.URL_CHARGING_SESSIONS;
import static com.ajai.chargingsession.constants.UrlConstants.URL_CHARGING_SESSIONS_SUMMARY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ajai.chargingsession.charging.dto.ChargingStationDTO;
import com.ajai.chargingsession.charging.handlers.ChargingSessionsHandler;
import com.ajai.chargingsession.charging.session.ChargingSession;
import com.ajai.chargingsession.charging.session.ChargingSessionSummary;
import io.swagger.annotations.ApiOperation;

/**
 * A RestController for handling the charging sessions. Apart from containing methods for starting
 * and stopping a charging session it also has methods that returns all the charging sessions and
 * also summarizing charging sessions over the last minute.
 * 
 * @author ajai
 *
 */
@ApiOperation(
    value = "Endpoints for initiating, viewing, terminating, and summarizing the charging sessions.")
@RestController
public class ChargingSessionController {

  private static final String DEFAULT_NO_OF_SECONDS = "1";

  private final ChargingSessionsHandler handler;

  /**
   * Creates a new instance of ChargingController.
   * 
   * @param handler instance of ChargingSessionsHandler
   */
  public ChargingSessionController(ChargingSessionsHandler handler) {
    this.handler = handler;
  }

  @ApiOperation(value = "View available charging sessions", response = Iterable.class)
  @GetMapping(path = URL_CHARGING_SESSIONS, produces = APPLICATION_JSON_VALUE)
  public HttpEntity<Iterable<ChargingSession>> getAllChargingSessions() {
    return new ResponseEntity<>(handler.getAllChargingSessions(), HttpStatus.OK);
  }

  @ApiOperation(value = "Create a new charging session", response = ChargingSession.class)
  @PostMapping(path = URL_CHARGING_SESSIONS, consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public HttpEntity<ChargingSession> startChargingSession(
      @Valid @RequestBody ChargingStationDTO chargingStationDTO) {
    return new ResponseEntity<>(handler.startChargingSession(chargingStationDTO),
        HttpStatus.CREATED);
  }

  @ApiOperation(value = "Stop a charging session", response = ChargingSession.class)
  @PutMapping(path = URL_CHARGING_SESSION, consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public HttpEntity<ChargingSession> stopChargingSession(
      @PathVariable(CHARGING_SESSION_ID) @NotBlank UUID chargingSessionId) {
    return new ResponseEntity<>(handler.stopChargingSession(chargingSessionId), HttpStatus.OK);
  }

  @ApiOperation(value = "View a summary of charging sessions",
      response = ChargingSessionSummary.class)
  @GetMapping(path = URL_CHARGING_SESSIONS_SUMMARY, produces = APPLICATION_JSON_VALUE)
  public HttpEntity<ChargingSessionSummary> getChargingSessionSummary(@Valid @RequestParam(
      value = SECONDS, defaultValue = DEFAULT_NO_OF_SECONDS) @NotBlank long seconds) {
    ChargingSessionSummary summary = new ChargingSessionSummary(handler.getChargingSessionSummary(seconds));
    return new ResponseEntity<>(summary, HttpStatus.OK);
  }

}
