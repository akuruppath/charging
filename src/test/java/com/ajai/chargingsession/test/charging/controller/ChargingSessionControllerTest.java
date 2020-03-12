package com.ajai.chargingsession.test.charging.controller;

import static com.ajai.chargingsession.constants.Constants.SECONDS;
import static com.ajai.chargingsession.constants.UrlConstants.URL_CHARGING_SESSIONS;
import static com.ajai.chargingsession.constants.UrlConstants.URL_CHARGING_SESSIONS_SUMMARY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import com.ajai.chargingsession.charging.controller.ChargingSessionController;
import com.ajai.chargingsession.charging.dto.ChargingStationDTO;
import com.ajai.chargingsession.charging.handlers.ChargingSessionsHandler;
import com.ajai.chargingsession.charging.session.ChargingSession;
import com.ajai.chargingsession.charging.session.StatusEnum;
import com.ajai.chargingsession.charging.session.ChargingSession.ChargingSessionBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

/**
 * 
 * Test class that contains tests for the ChargingSessionController class. The
 * ChargingSessionsHandler is mocked for this test.
 * 
 * So that every test will be done in isolation the application-context will be destroyed after
 * every test.
 * 
 * @author ajai
 *
 */
@WebMvcTest(controllers = ChargingSessionController.class)
@Import(ChargingSessionsHandler.class)
class ChargingSessionControllerTest {

  private final Random random = new Random();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ChargingSessionsHandler handler;

  @Test
  @DirtiesContext
  void testGetAllChargingSessions() throws Exception {

    String stationId = "ABC-" + random.nextInt();

    ChargingStationDTO chargingStationDTO = new ChargingStationDTO(stationId);

    ChargingSession chargingSession = getStartedChargingSession.apply(stationId);

    Mockito.when(handler.startChargingSession(chargingStationDTO)).thenReturn(chargingSession);

    Mockito.when(handler.getAllChargingSessions())
        .thenReturn(Collections.singleton(chargingSession));

    this.mockMvc
        .perform(post(URL_CHARGING_SESSIONS).contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(chargingStationDTO)))
        .andExpect(status().isCreated());

    this.mockMvc.perform(get(URL_CHARGING_SESSIONS).accept(APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.length()", equalTo(1)));
  }

  @Test
  @DirtiesContext
  void testStartChargingSession() throws Exception {

    String stationId = "ABC-" + random.nextInt();

    ChargingStationDTO chargingStationDTO = new ChargingStationDTO(stationId);

    ChargingSession chargingSession = getStartedChargingSession.apply(stationId);

    Mockito.when(handler.startChargingSession(chargingStationDTO)).thenReturn(chargingSession);

    Mockito.when(handler.getAllChargingSessions())
        .thenReturn(Collections.singleton(chargingSession));

    this.mockMvc
        .perform(post(URL_CHARGING_SESSIONS).contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(chargingStationDTO)))
        .andExpect(status().isCreated());

    this.mockMvc.perform(get(URL_CHARGING_SESSIONS).accept(APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.length()", equalTo(1)))

        // check whether all the expected fields exist in the response.
        .andExpect(jsonPath("$.[0].id").exists()).andExpect(jsonPath("$.[0].stationId").exists())
        .andExpect(jsonPath("$.[0].stationId", equalTo(stationId)))
        .andExpect(jsonPath("$.[0].startedAt").exists())
        .andExpect((jsonPath("$.[0].stoppedAt").doesNotExist()))
        .andExpect((jsonPath("$.[0].status", equalTo("IN_PROGRESS")))).andExpect(status().isOk());
  }

  @Test
  @DirtiesContext
  public void testStartChargingSessionWithEmptyStationId() throws Exception {

    this.mockMvc
        .perform(post(URL_CHARGING_SESSIONS).contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(new ChargingStationDTO(" "))))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DirtiesContext
  void testStopChargingSession() throws Exception {

    String stationId = "ABC-" + random.nextInt();

    ChargingStationDTO chargingStationDTO = new ChargingStationDTO(stationId);

    ChargingSession chargingSession = getStoppedChargingSession.apply(stationId);

    UUID chargingSessionId = chargingSession.getId();

    Mockito.when(handler.stopChargingSession(chargingSessionId)).thenReturn(chargingSession);

    Mockito.when(handler.getAllChargingSessions())
        .thenReturn(Collections.singleton(chargingSession));

    this.mockMvc
        .perform(post(URL_CHARGING_SESSIONS).contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(chargingStationDTO)))
        .andExpect(status().isCreated());

    this.mockMvc.perform(get(URL_CHARGING_SESSIONS).accept(APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.length()", equalTo(1)))

        // check whether all the expected fields exist in the response.
        .andExpect(jsonPath("$.[0].id").exists()).andExpect(jsonPath("$.[0].stationId").exists())
        .andExpect(jsonPath("$.[0].stationId", equalTo(stationId)))
        .andExpect(jsonPath("$.[0].startedAt").exists())
        .andExpect((jsonPath("$.[0].stoppedAt").exists()))
        .andExpect((jsonPath("$.[0].stoppedAt").isNotEmpty()))
        .andExpect((jsonPath("$.[0].status", equalTo("FINISHED")))).andExpect(status().isOk());

  }

  @Test
  @DirtiesContext
  void testGetChargingSessionSummary() throws Exception {

    Map<StatusEnum, Long> summaryMap = Maps.newHashMap();
    summaryMap.put(StatusEnum.IN_PROGRESS, 1L);
    summaryMap.put(StatusEnum.FINISHED, 1L);

    Mockito.when(handler.getChargingSessionSummary(1)).thenReturn(summaryMap);

    this.mockMvc
        .perform(get(URL_CHARGING_SESSIONS_SUMMARY).queryParam(SECONDS, "1")
            .accept(APPLICATION_JSON_VALUE))
        .andExpect((jsonPath("$.totalCount", equalTo(2))))
        .andExpect((jsonPath("$.startedCount", equalTo(1))))
        .andExpect((jsonPath("$.stoppedCount", equalTo(1)))).andExpect(status().isOk());
  }

  @Test
  @DirtiesContext
  public void testGetSummaryWhenNoChargingSessionsExist() throws Exception {
    this.mockMvc.perform(get(URL_CHARGING_SESSIONS_SUMMARY).accept(APPLICATION_JSON_VALUE))
        .andExpect((jsonPath("$.totalCount", equalTo(0))))
        .andExpect((jsonPath("$.startedCount", equalTo(0))))
        .andExpect((jsonPath("$.stoppedCount", equalTo(0)))).andExpect(status().isOk());
  }

  Function<String, ChargingSession> getStartedChargingSession = stationId -> {
    return new ChargingSessionBuilder().with(chargingSessionBuilder -> {
      chargingSessionBuilder.id = UUID.randomUUID();
      chargingSessionBuilder.stationId = stationId;
      chargingSessionBuilder.startedAt = LocalDateTime.now();
      chargingSessionBuilder.status = StatusEnum.IN_PROGRESS;
    }).build();
  };

  Function<String, ChargingSession> getStoppedChargingSession = stationId -> {
    return new ChargingSessionBuilder().with(chargingSessionBuilder -> {
      chargingSessionBuilder.id = UUID.randomUUID();
      chargingSessionBuilder.stationId = stationId;
      chargingSessionBuilder.startedAt = LocalDateTime.now().minusMinutes(30);
      chargingSessionBuilder.stoppedAt = LocalDateTime.now();
      chargingSessionBuilder.status = StatusEnum.FINISHED;
    }).build();
  };

}
