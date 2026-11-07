package portfolio.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.UUID;
import portfolio.config.TestConfig;
import portfolio.requests.Browse;
import portfolio.requests.Create;
import portfolio.requests.Search;

public class ComputerDatabaseSimulation extends Simulation {

  private final HttpProtocolBuilder httpProtocol =
      http.baseUrl(TestConfig.BASE_URL)
          .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
          .acceptLanguageHeader("fr-FR,fr;q=0.9,en;q=0.8")
          .acceptEncodingHeader("gzip, deflate")
          .userAgentHeader("Gatling - portfolio QA");

  // 80 % des utilisateurs consultent, 20 % créent des données : un mix réaliste
  private final ScenarioBuilder visitors =
      scenario("Visiteurs")
          .exec(session -> session.set("userId", UUID.randomUUID().toString().substring(0, 8)))
          .exec(Search.SEARCH, Browse.BROWSE);

  private final ScenarioBuilder editors =
      scenario("Éditeurs")
          .exec(session -> session.set("userId", UUID.randomUUID().toString().substring(0, 8)))
          .exec(Search.SEARCH, Create.CREATE);

  private PopulationBuilder[] injection() {
    double rate = TestConfig.USERS_PER_SEC;
    Duration ramp = Duration.ofSeconds(60);
    Duration plateau = Duration.ofSeconds(TestConfig.DURATION_SEC);

    return switch (TestConfig.PROFILE) {
      // Charge nominale : montée progressive puis palier
      case "load" -> new PopulationBuilder[] {
        visitors.injectOpen(
            rampUsersPerSec(0.1).to(rate * 0.8).during(ramp),
            constantUsersPerSec(rate * 0.8).during(plateau)),
        editors.injectOpen(
            rampUsersPerSec(0.1).to(rate * 0.2).during(ramp),
            constantUsersPerSec(rate * 0.2).during(plateau))
      };
      // Recherche du point de rupture : paliers croissants
      case "stress" -> new PopulationBuilder[] {
        visitors.injectOpen(
            incrementUsersPerSec(1)
                .times(5)
                .eachLevelLasting(30)
                .separatedByRampsLasting(10)
                .startingFrom(1)),
        editors.injectOpen(constantUsersPerSec(0.5).during(Duration.ofSeconds(200)))
      };
      // Smoke : vérifie que les scénarios fonctionnent, avec très peu d'utilisateurs
      default -> new PopulationBuilder[] {
        visitors.injectOpen(atOnceUsers(1), rampUsers(4).during(Duration.ofSeconds(10))),
        editors.injectOpen(atOnceUsers(1))
      };
    };
  }

  {
    setUp(injection())
        .protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile(95.0).lt(TestConfig.P95_MAX_MS),
            global().failedRequests().percent().lt(TestConfig.ERROR_RATE_MAX));
  }
}
