package portfolio.config;

/**
 * Paramètres du test, modifiables en ligne de commande sans toucher au code :
 *   mvn gatling:test -Dprofile=load -DusersPerSec=5 -DbaseUrl=https://mon-app
 */
public final class TestConfig {

  private TestConfig() {}

  /** Application ciblée. Par défaut : site de démo fourni par Gatling. */
  public static final String BASE_URL =
      System.getProperty("baseUrl", "https://api-ecomm.gatling.io");

  /** Profil de charge : smoke, load ou stress. */
  public static final String PROFILE = System.getProperty("profile", "smoke");

  /** Débit cible (nouveaux utilisateurs par seconde) pour le profil load. */
  public static final double USERS_PER_SEC =
      Double.parseDouble(System.getProperty("usersPerSec", "2"));

  /** Durée du palier de charge, en secondes, pour le profil load. */
  public static final long DURATION_SEC =
      Long.parseLong(System.getProperty("durationSec", "120"));

  /** SLA : 95e percentile max (ms) et taux d'erreur max (%). */
  public static final int P95_MAX_MS = Integer.parseInt(System.getProperty("p95MaxMs", "1500"));
  public static final double ERROR_RATE_MAX = Double.parseDouble(System.getProperty("errorRateMax", "1"));
}
