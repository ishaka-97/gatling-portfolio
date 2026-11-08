# Gatling Portfolio – Tests de performance

![Tests de performance Gatling](https://github.com/ishaka-97/gatling-portfolio/actions/workflows/gatling.yml/badge.svg)

Tests de performance réalisés avec **Gatling 3 (Java DSL)** et **Maven**, exécutés sur **GitHub Actions**.
Application ciblée : `https://api-ecomm.gatling.io`, API e-commerce de démonstration mise à disposition par Gatling.

📊 **[Voir le dernier rapport Gatling](https://ishaka-97.github.io/gatling-portfolio/)**

## Ce que montre ce projet

- **Scénarios modulaires** : chaque appel d'API est une brique réutilisable (`requests/`)
- **Mix de trafic réaliste** : 70 % de visiteurs anonymes, 30 % de clients qui se connectent
- **Corrélation** : le jeton d'authentification est extrait de la réponse (`jmesPath`) puis réinjecté dans l'en-tête `Authorization`
- **Données de test** : comptes alimentés par un feeder JSON
- **Trois profils de charge** sélectionnables sans modifier le code :
  - `smoke` : vérification rapide des scénarios (quelques utilisateurs)
  - `load` : montée progressive puis palier au débit cible
  - `stress` : paliers croissants pour trouver le point de rupture
- **SLA automatisés** : le build échoue si le 95e percentile dépasse 1,5 s ou si le taux d'erreur dépasse 1 %
- **CI/CD** : smoke à chaque push, lancement manuel paramétrable, rapport publié sur GitHub Pages

## Structure

```
.github/workflows/gatling.yml        Pipeline CI
src/test/java/portfolio/
  config/TestConfig.java             Paramètres (URL, profil, débit, SLA)
  requests/                          Briques d'appels API (SessionApi, AuthApi)
  simulations/                       Simulation et profils d'injection
src/test/resources/data/users.json   Comptes de test
```

## Lancer en local

Prérequis : Java 17+ et Maven.

```bash
mvn gatling:test                                    # profil smoke
mvn gatling:test -Dprofile=load -DusersPerSec=3     # charge nominale
mvn gatling:test -Dprofile=stress                   # paliers croissants
mvn gatling:test -DbaseUrl=https://mon-app.test     # autre environnement
```

Le rapport HTML est généré dans `target/gatling/`.

## Lancer depuis GitHub

Onglet **Actions** → **Tests de performance Gatling** → **Run workflow**, puis choisir le profil et le débit.

> Les charges sont volontairement modérées : l'application ciblée est un service de démonstration partagé.
> Pour des tests plus lourds, il suffit de pointer `baseUrl` vers un environnement dédié.

## Auteur

**Siaka KONE** – Ingénieur QA (5 ans d'expérience) : tests de charge Gatling, automatisation Playwright/TypeScript, CI/CD.
