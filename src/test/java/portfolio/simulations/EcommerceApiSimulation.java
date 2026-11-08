package portfolio.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import portfolio.config.TestConfig;
import portfolio.requests.AuthApi;
import portfolio.requests.SessionApi;

public class EcommerceApiSimulation extends Simulation {

  private final HttpProtocolBuilder httpProtocol =
      http.baseUrl(TestConfig.BASE_URL)
          .acceptHeader("application/json")
          .acceptEncodingHeader("gzip, deflate")
          .userAgentHeader("Gatling - portfolio QA");

  // Visiteurs anonymes : ouvrent une session puis naviguent
  private final ScenarioBuilder visitors =
      scenario("Visiteurs anonymes")
          .exec(SessionApi.OPEN_SESSION)
          .pause(1, 3)
          .exec(SessionApi.OPEN_SESSION);

  // Clients : session, connexion, puis appel authentifié avec le jeton récupéré
  private final ScenarioBuilder customers =
      scenario("Clients connectés")
          .exec(SessionApi.OPEN_SESSION)
          .pause(1, 2)
          .exec(AuthApi.LOGIN)
          .pause(1, 2)
          .exec(SessionApi.AUTHENTICATED_SESSION);

  private PopulationBuilder[] injection() {
    double rate = TestConfig.USERS_PER_SEC;
    Duration ramp = Duration.ofSeconds(60);
    Duration plateau = Duration.ofSeconds(TestConfig.DURATION_SEC);

    return switch (TestConfig.PROFILE) {
      // Charge nominale : montée progressive puis palier (70 % visiteurs / 30 % clients)
      case "load" -> new PopulationBuilder[] {
        visitors.injectOpen(
            rampUsersPerSec(0.1).to(rate * 0.7).during(ramp),
            constantUsersPerSec(rate * 0.7).during(plateau)),
        customers.injectOpen(
            rampUsersPerSec(0.1).to(rate * 0.3).during(ramp),
            constantUsersPerSec(rate * 0.3).during(plateau))
      };
      // Paliers croissants pour observer la dégradation
      case "stress" -> new PopulationBuilder[] {
        visitors.injectOpen(
            incrementUsersPerSec(1)
                .times(5)
                .eachLevelLasting(30)
                .separatedByRampsLasting(10)
                .startingFrom(1)),
        customers.injectOpen(constantUsersPerSec(0.5).during(Duration.ofSeconds(200)))
      };
      // Smoke : vérifie que les scénarios fonctionnent
      default -> new PopulationBuilder[] {
        visitors.injectOpen(atOnceUsers(1), rampUsers(4).during(Duration.ofSeconds(10))),
        customers.injectOpen(atOnceUsers(1), rampUsers(2).during(Duration.ofSeconds(10)))
      };
    };
  }

  {
    setUp(injection())
        .protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile(95.0).lt(TestConfig.P95_MAX_MS),
            global().failedRequests().percent().lt(TestConfig.ERROR_RATE_MAX),
            details("Connexion").failedRequests().percent().lt(TestConfig.ERROR_RATE_MAX));
  }
}
