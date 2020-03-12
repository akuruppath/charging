package com.ajai.chargingsession.constants;

import static com.ajai.chargingsession.constants.Constants.CHARGING_SESSION_ID;

/**
 * Represents a utility class that contains all the URLs.
 * 
 * @author ajai
 *
 */
public final class UrlConstants {

  private UrlConstants() {
    // EMPTY
  }

  private static final String URL_SUMMARY = "/summary";

  public static final String URL_CHARGING_SESSIONS = "/chargingSessions";

  public static final String URL_CHARGING_SESSION =
      URL_CHARGING_SESSIONS + "/" + "{" + CHARGING_SESSION_ID + "}";

  public static final String URL_CHARGING_SESSIONS_SUMMARY = URL_CHARGING_SESSIONS + URL_SUMMARY;

}
