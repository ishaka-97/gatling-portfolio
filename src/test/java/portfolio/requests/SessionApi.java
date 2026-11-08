package portfolio.requests;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.ChainBuilder;

/** Ouverture d'une session sur l'API (utilisateur anonyme ou connecté). */
public final class SessionApi {

  private SessionApi() {}

  public static final ChainBuilder OPEN_SESSION =
      exec(
          http("Ouvrir une session")
              .get("/session")
              .check(status().is(200)));

  public static final ChainBuilder AUTHENTICATED_SESSION =
      exec(
          http("Session authentifiée")
              .get("/session")
              .header("Authorization", "#{accessToken}")
              .check(status().is(200)));
}
