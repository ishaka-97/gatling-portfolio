package portfolio.requests;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.ChainBuilder;

/** Création d'un ordinateur via le formulaire (requête POST). */
public final class Create {

  private Create() {}

  public static final ChainBuilder CREATE =
      group("Création").on(
          exec(http("Formulaire de création").get("/computers/new").check(status().is(200)))
              .pause(2, 4)
              .exec(
                  http("Enregistrer l'ordinateur")
                      .post("/computers")
                      .formParam("name", "Portfolio QA #{userId}")
                      .formParam("introduced", "2026-01-15")
                      .formParam("discontinued", "")
                      .formParam("company", "37")
                      .check(status().is(200))));
}
