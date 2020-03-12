package com.ajai.chargingsession.test.handlers;

import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import com.ajai.chargingsession.charging.dto.ChargingStationDTO;
import com.ajai.chargingsession.charging.handlers.ChargingSessionsHandler;
import com.ajai.chargingsession.charging.session.ChargingSession;
import com.ajai.chargingsession.charging.session.StatusEnum;
import com.google.common.collect.Sets;

/**
 * 
 * Test class that tests the ChargingSessionsHandler. It uses the actual spring bean as the
 * application. It tries to mimic the real world scenario where multiple POST and PUT requests will
 * be made concurrently.
 * 
 * 
 * @author ajai
 *
 */
@SpringBootTest
@Import(ChargingSessionsHandler.class)
class ChargingSessionsHandlerTest {

  @Autowired
  private ChargingSessionsHandler chargingSessionsHandler;

  @Test
  @DirtiesContext
  void testStartChargingSessions() {

    List<ChargingSession> chargingSessions = startAndGetChargingSessions();

    assertFalse(chargingSessions.isEmpty(), () -> "Expected 5 chargings sessions to be created.");
    assertTrue(chargingSessions.size() == 5, () -> "Expected 5 chargings sessions to be created.");

    chargingSessions.forEach(session -> assertTrue(session.getStatus() == StatusEnum.IN_PROGRESS,
        () -> "Expected the newly created charging session to have status "
            + StatusEnum.IN_PROGRESS));

    Set<String> actualChargingStationIds = chargingSessions.stream()
        .map(session -> session.getStationId()).collect(Collectors.toSet());

    Set<String> expectedChargingStationIds =
        Sets.newHashSet("ABC-1", "ABC-2", "ABC-3", "ABC-4", "ABC-5");

    assertEquals(expectedChargingStationIds, actualChargingStationIds,
        () -> "Expected station ids to match.");
  }

  @Test
  @DirtiesContext
  void testStopChargingSessions() {

    stopChargingSessions(5);
  }


  @Test
  @DirtiesContext
  void testChargingSessionSummary() {

    stopChargingSessions(2);

    assertThrows(IllegalArgumentException.class,
        () -> chargingSessionsHandler.getChargingSessionSummary(0));


    assertThrows(IllegalArgumentException.class,
        () -> chargingSessionsHandler.getChargingSessionSummary(-1));

    assertThrows(IllegalArgumentException.class,
        () -> chargingSessionsHandler.getChargingSessionSummary(61));

    Map<StatusEnum, Long> chargingSessionSummary =
        chargingSessionsHandler.getChargingSessionSummary(1);

    assertTrue(chargingSessionSummary.size() == 2, () -> "Expected 2 groups to be present.");
    assertTrue(chargingSessionSummary.get(StatusEnum.IN_PROGRESS) == 3,
        () -> "Expected 3 charging sessions to be in progress.");
    assertTrue(chargingSessionSummary.get(StatusEnum.FINISHED) == 2,
        () -> "Expected 2 charging sessions to be finished.");
  }


  private void stopChargingSessions(int chargingSessionsToBeStopped) {

    startAndGetChargingSessions().stream().limit(chargingSessionsToBeStopped)
        .map(session -> session.getId())
        .map(chargingSessionId -> chargingSessionsHandler.stopChargingSession(chargingSessionId))
        .forEach(chargingSession -> assertTrue(chargingSession.getStatus() == StatusEnum.FINISHED,
            () -> "Expected status of the stopped charging session to be FINISHED."));
  }

  private List<ChargingSession> startAndGetChargingSessions() {
    IntStream.rangeClosed(1, 5).parallel().forEach(index -> {
      chargingSessionsHandler.startChargingSession(new ChargingStationDTO("ABC-" + index));
    });

    return stream(chargingSessionsHandler.getAllChargingSessions().spliterator(), false)
        .collect(Collectors.toList());
  }

}
