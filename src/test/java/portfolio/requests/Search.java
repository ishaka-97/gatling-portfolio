package portfolio.requests;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;

/** Recherche d'un ordinateur puis consultation de sa fiche. */
public final class Search {

  private Search() {}

  private static final FeederBuilder<String> FEEDER = csv("data/search.csv").random();

  public static final ChainBuilder SEARCH =
      group("Recherche").on(
          exec(http("Accueil").get("/computers").check(status().is(200)))
              .pause(1, 3)
              .feed(FEEDER)
              .exec(
                  http("Rechercher")
                      .get("/computers")
                      .queryParam("f", "#{searchCriterion}")
                      .check(
                          status().is(200),
                          css("a:contains('#{searchComputerName}')", "href").saveAs("computerUrl")))
              .pause(1, 2)
              .exec(http("Fiche ordinateur").get("#{computerUrl}").check(status().is(200))));
}
