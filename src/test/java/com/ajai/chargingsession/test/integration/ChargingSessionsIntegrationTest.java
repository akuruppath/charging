package com.ajai.chargingsession.test.integration;

import static com.ajai.chargingsession.constants.Constants.SECONDS;
import static com.ajai.chargingsession.constants.UrlConstants.URL_CHARGING_SESSION;
import static com.ajai.chargingsession.constants.UrlConstants.URL_CHARGING_SESSIONS;
import static com.ajai.chargingsession.constants.UrlConstants.URL_CHARGING_SESSIONS_SUMMARY;
import static com.jayway.jsonpath.JsonPath.read;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.ajai.chargingsession.charging.dto.ChargingStationDTO;
import com.ajai.chargingsession.charging.session.StatusEnum;
import com.google.gson.Gson;

/**
 * 
 * Test class that tests the entire functionality of the application i.e an Integration test. It
 * uses the actual spring beans as the application. The tests are done in the same application
 * context. It tries to mimic the real world scenario where multiple POST and PUT requests will be
 * made concurrently.
 * 
 * 
 * @author ajai
 *
 */
@SpringBootTest
class ChargingSessionsIntegrationTest {

  private MockMvc mockMvc;

  private final Gson gson = new Gson();

  private final Random random = new Random();

  @Autowired
  private WebApplicationContext wac;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }


  @Test
  void testChargingSessions() throws Exception {

    final Map<Integer, String> idMap = new ConcurrentSkipListMap<>();

    /* create a few charging sessions */

    IntStream.range(0, 10).parallel().forEach(index -> {

      String chargingSessionid = startChargingSessions.apply(createAndGetChargingStationDTO.get());
      assertNotNull(chargingSessionid, () -> "Expected a non-null charging session id.");
      idMap.put(index, chargingSessionid);
    });


    /* get them back and do some checks */

    this.mockMvc.perform(get(URL_CHARGING_SESSIONS).accept(APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.length()", equalTo(10)))

        // check whether all the expected fields exist in the response.
        .andExpect(jsonPath("$[*].id").exists()).andExpect(
            jsonPath("$[*].stationId").exists())
        .andExpect(jsonPath("$[*].startedAt").exists())
        .andExpect((jsonPath("$[*].stoppedAt").doesNotExist()))
        .andExpect((jsonPath("$[*].status",
            contains(Stream.generate(() -> StatusEnum.IN_PROGRESS.getStatus()).limit(10)
                .collect(Collectors.toList()).toArray()))))
        .andExpect(status().isOk());

    /* stop a few charging sessions */

    IntStream.range(5, 10).parallel().forEach(index -> {
      stopChargingSessions.accept(idMap.get(index));
    });


    /* get the summary for the last 1 minute */

    this.mockMvc
        .perform(get(URL_CHARGING_SESSIONS_SUMMARY).queryParam(SECONDS, "60")
            .accept(APPLICATION_JSON_VALUE))
        .andExpect((jsonPath("$.totalCount", equalTo(10))))
        .andExpect((jsonPath("$.startedCount", equalTo(5))))
        .andExpect((jsonPath("$.stoppedCount", equalTo(5)))).andExpect(status().isOk());

    /* sleep for 2 seconds */

    Thread.sleep(2000);

    /* create more charging sessions */

    IntStream.range(10, 15).parallel().forEach(index -> {

      String chargingSessionid = startChargingSessions.apply(createAndGetChargingStationDTO.get());
      assertNotNull(chargingSessionid, () -> "Expected a non-null charging session id.");
      idMap.put(index, chargingSessionid);
    });

    /* stop a few more charging sessions */

    IntStream.range(13, 15).parallel().forEach(i -> {
      stopChargingSessions.accept(idMap.get(i));
    });

    /*
     * get summary for last 1 second;in case the request param is not specified the default value
     * would be 1
     */

    this.mockMvc.perform(get(URL_CHARGING_SESSIONS_SUMMARY).accept(APPLICATION_JSON_VALUE))
        .andExpect((jsonPath("$.totalCount", equalTo(5))))
        .andExpect((jsonPath("$.startedCount", equalTo(3))))
        .andExpect((jsonPath("$.stoppedCount", equalTo(2)))).andExpect(status().isOk());

  }

  @Test
  void testStopNonExistentChargingSession() throws Exception {

    this.mockMvc.perform(
        put(URL_CHARGING_SESSION, UUID.randomUUID().toString()).contentType(APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  void testOutOfRangeSecondsForChargingSessionSummary() throws Exception {

    this.mockMvc.perform(
        get(URL_CHARGING_SESSIONS_SUMMARY).queryParam(SECONDS, "0").accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());

    this.mockMvc.perform(
        get(URL_CHARGING_SESSIONS_SUMMARY).queryParam(SECONDS, "61").accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }


  private Supplier<String> createAndGetChargingStationDTO =
      () -> gson.toJson(new ChargingStationDTO("ABC-" + random.nextInt()));


  private Function<String, String> startChargingSessions = baseStationName -> {

    MvcResult result;
    try {
      result =
          this.mockMvc
              .perform(post(URL_CHARGING_SESSIONS).contentType(APPLICATION_JSON_VALUE)
                  .content(baseStationName))
              .andDo(print()).andExpect(status().isCreated()).andReturn();
      return read(result.getResponse().getContentAsString(), "$.id");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  };


  private Consumer<String> stopChargingSessions = chargingSessionId -> {
    try {

      this.mockMvc
          .perform(put(URL_CHARGING_SESSION, chargingSessionId).contentType(APPLICATION_JSON_VALUE))
          .andDo(print())
          .andExpect((jsonPath("$.status", equalTo(StatusEnum.FINISHED.getStatus()))))
          .andExpect((jsonPath("$.stoppedAt").exists()))
          .andExpect((jsonPath("$.stoppedAt").isNotEmpty())).andExpect(status().isOk());

    } catch (Exception e) {
      e.printStackTrace();
    }
  };

}
