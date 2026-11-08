package portfolio.requests;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;

/** Authentification : récupère un jeton et le garde dans la session Gatling. */
public final class AuthApi {

  private AuthApi() {}

  private static final FeederBuilder<Object> USERS = jsonFile("data/users.json").circular();

  public static final ChainBuilder LOGIN =
      feed(USERS)
          .exec(
              http("Connexion")
                  .post("/login")
                  .asFormUrlEncoded()
                  .formParam("username", "#{username}")
                  .formParam("password", "#{password}")
                  .check(
                      status().is(200),
                      jmesPath("accessToken").exists(),
                      jmesPath("accessToken").saveAs("accessToken")))
          // Si la connexion échoue, l'utilisateur virtuel s'arrête ici :
          // inutile d'appeler des routes protégées sans jeton
          .exitHereIfFailed();
}