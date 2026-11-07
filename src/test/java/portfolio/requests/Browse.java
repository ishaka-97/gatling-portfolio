package portfolio.requests;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.ChainBuilder;

/** Navigation dans les pages de la liste (pagination). */
public final class Browse {

  private Browse() {}

  public static final ChainBuilder BROWSE =
      group("Navigation").on(
          repeat(4, "page").on(
              exec(
                      http("Page de liste")
                          .get("/computers")
                          .queryParam("p", "#{page}")
                          .check(status().is(200), css("table.computers").exists()))
                  .pause(1)));
}
