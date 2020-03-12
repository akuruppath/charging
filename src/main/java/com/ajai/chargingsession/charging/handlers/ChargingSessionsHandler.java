package com.ajai.chargingsession.charging.handlers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import com.ajai.chargingsession.charging.dto.ChargingStationDTO;
import com.ajai.chargingsession.charging.session.ChargingSession;
import com.ajai.chargingsession.charging.session.StatusEnum;
import com.ajai.chargingsession.charging.session.ChargingSession.ChargingSessionBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

/**
 * Handler for ChargingSessions.
 * 
 * <p>
 * This class provides a variety of methods to read, write, and summarize the charging session
 * information to the backing {@link com.google.common.collect.Table}. The reads from and writes to
 * this data-structure are thread-safe.
 * </p>
 * 
 * @author ajai
 *
 */
@Component
public class ChargingSessionsHandler {

  @Value("${seconds.lower.limit}")
  private long secondsLowerLimit;

  @Value("${seconds.higher.limit}")
  private long secondsHigherLimit;

  private final Table<LocalDateTime, UUID, ChargingSession> chargingSessionTable;
  private final ReadWriteLock readWriteLock;
  private final Lock readLock;
  private final Lock writeLock;

  /**
   * Creates an instance of ChargingSessionsHandler along with the Table and locks.
   */
  public ChargingSessionsHandler() {
    chargingSessionTable = TreeBasedTable.create();
    readWriteLock = new ReentrantReadWriteLock();
    readLock = readWriteLock.readLock();
    writeLock = readWriteLock.writeLock();
  }

  /**
   * Thread safe method that returns all the charging sessions.
   * 
   * @return Iterable of charging sessions.
   */
  public Iterable<ChargingSession> getAllChargingSessions() {

    readLock.lock();
    try {
      return ImmutableList.<ChargingSession>builder().addAll(chargingSessionTable.values()).build();
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Thread-safe method to create and store a new charging session.
   * 
   * @param chargingStationDTO the DTO object that has the charging station information
   * 
   * @return ChargingSession the newly started chargingSession.
   */
  public ChargingSession startChargingSession(ChargingStationDTO chargingStationDTO) {

    writeLock.lock();
    try {
      UUID chargingSessionId = UUID.randomUUID();
      LocalDateTime chargingStartDateTime = LocalDateTime.now();
      ChargingSession newChargingSession =
          new ChargingSessionBuilder().with(chargingSessionBuilder -> {
            chargingSessionBuilder.id = chargingSessionId;
            chargingSessionBuilder.stationId = chargingStationDTO.getStationId();
            chargingSessionBuilder.startedAt = chargingStartDateTime;
            chargingSessionBuilder.status = StatusEnum.IN_PROGRESS;
          }).build();

      chargingSessionTable.put(chargingStartDateTime, chargingSessionId, newChargingSession);
      ChargingSession chargingSession = chargingSessionTable.get(chargingStartDateTime, chargingSessionId);
      return chargingSession;

    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Thread-safe method to stop a charging session.
   * 
   * @param chargingSessionId the charging session id
   * @return Collection chargingSessions that were updated.
   * 
   * @throws IllegalArgumentException if no charging session exists for this id.
   */
  public ChargingSession stopChargingSession(UUID chargingSessionId) {

    writeLock.lock();
    try {
      Assert.state(chargingSessionTable.containsColumn(chargingSessionId),
          () -> "Invalid chargingSessionId [" + chargingSessionId + "] received");

      Map<LocalDateTime, ChargingSession> column = chargingSessionTable.column(chargingSessionId);
      column.replaceAll((localDateTime, chargingSession) -> {
        if (chargingSession.getStatus().equals(StatusEnum.IN_PROGRESS)) {
          chargingSession.setStatus(StatusEnum.FINISHED);
          chargingSession.setStoppedAt(LocalDateTime.now());
        }
        return chargingSession;
      });

      for (Entry<LocalDateTime, ChargingSession> entry : column.entrySet()) {
        chargingSessionTable.put(entry.getKey(), chargingSessionId, entry.getValue());
      }

      return chargingSessionTable.column(chargingSessionId).values().iterator().next();

    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Returns a summary of the charging sessions from second(s) ago categorized according to the
   * charging status.
   * 
   * @param seconds the number of seconds ago.
   * 
   * @return {@code Map<StatusEnum, Long>} summary of the charging sessions
   */
  public Map<StatusEnum, Long> getChargingSessionSummary(long seconds) {

    readLock.lock();
    try {
      Assert.isTrue(seconds >= secondsLowerLimit && seconds <= secondsHigherLimit,
          () -> "The number of seconds specified should be between " + secondsLowerLimit + " and "
              + secondsHigherLimit);

      SortedMap<LocalDateTime, Map<UUID, ChargingSession>> rowMap =
          (SortedMap<LocalDateTime, Map<UUID, ChargingSession>>) chargingSessionTable.rowMap();

      LocalDateTime now = LocalDateTime.now();

      SortedMap<LocalDateTime, Map<UUID, ChargingSession>> subMap =
          rowMap.subMap(now.minusSeconds(seconds), now);

      return ImmutableMap.copyOf(subMap.values().stream().flatMap(e -> e.values().stream())
          .collect(Collectors.groupingBy(ChargingSession::getStatus, Collectors.counting())));

    } finally {
      readLock.unlock();
    }
  }

}
