# KOLOK

![bot_gif](https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExa2hhaWZ1M2ZxZnAzZHJnZTN5emVwb3V0em5sN2VyZ3Qwa2Rtdm1veSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/XYZ9OXuxBNvPA8APXs/giphy.gif)

`kolok` est une application **Spring Boot + Kotlin** qui génère des plannings (rotations de responsabilités), et publie le résultat sur **Discord**.

Le projet est conçu comme un backend unique (pas de front), déployable facilement en **Docker**. Les données (catalogue, plannings) sont persistées en **JSON** dans un dossier `data/`.

## A Voir

https://ktor.io/

## Fonctionnalités
- Génération de planning à partir :
  - d’un catalogue de responsabilités
  - d’une liste de responsables/disponibilités
  - de règles de rotation (équité, couverture, etc.)
- Publication sur Discord (bot) dans un salon configuré
- Scheduler hebdomadaire (déclenchement automatique)
- Persistance JSON des plannings et du catalogue (dossier `data/`)
- Endpoints Actuator (healthcheck) pour Docker/ops

## Prérequis

### Docker
- Docker Desktop (ou Docker Engine)

## Configuration
La configuration applicative est basée sur des **variables d’environnement** (voir `src/main/resources/application.properties`).

Variables importantes :
- `DISCORD_BOT_TOKEN` : token du bot Discord
- `DISCORD_PLANNING_CHANNEL_ID` : ID du salon où publier
- `JSON_DIR_PATH` : dossier contenant `catalog.json` et `plannings.json` (par défaut `data`)

### Fichier `.env`
Un fichier `.env` est supporté par `compose.yaml` et est **gitignored** (recommandé pour les secrets).

Exemple :
```dotenv
DISCORD_BOT_TOKEN=...
DISCORD_PLANNING_CHANNEL_ID=...
JSON_DIR_PATH=data
```

## Démarrer l’application

### 1) En local (Gradle Wrapper)
Commandes (à lancer à la racine du repo) :
```shell
./gradlew.bat --no-daemon clean test bootJar
./gradlew.bat --no-daemon bootRun
```

L’application écoute par défaut sur `http://localhost:8080`.

### 2) En Docker (Compose)
Build + run :
```shell
docker compose up --build
```

Healthcheck :
- `GET http://localhost:8080/actuator/health`

#### Avec la base Postgres (optionnel)
Le repo fournit un override `compose.db.yaml` avec un service `db` sous profil `db`.

Démarrage :
```shell
docker compose -f compose.yaml -f compose.db.yaml --profile db up --build
```

> Important : si tu ajoutes `depends_on: db` mais que le profil `db` n’est pas activé, Compose considère `db` comme “undefined”.

## Architecture (vue d’ensemble)
Le code suit une organisation **hexagonale/clean** :

- **Domain** (pur, sans Spring) : modèle métier et règles de rotation
  - `src/main/kotlin/.../domain/`
- **Application** : services (use-cases) et ports
  - `src/main/kotlin/.../application/`
- **Adapters** : entrées/sorties techniques
  - `src/main/kotlin/.../adapters/in/` (Discord/REST/scheduler)
  - `src/main/kotlin/.../adapters/infrastructure/` (persistence, clients externes, etc.)
- **Bootstrap / Wiring** : composition des dépendances
  - `src/main/kotlin/.../bootstrap/Wiring.kt`

Point d’entrée Spring Boot :
- `src/main/kotlin/cat/ohmushi/kolok/planning/KolokApplication.kt`

Persistance JSON actuelle (par défaut) :
- catalogue : `data/catalog.json`
- plannings : `data/plannings.json`

## Tests
- Tests : `src/test/kotlin/...`
- Lancer :
```shell
./gradlew.bat --no-daemon test
```

## CI/CD
- CI : `/.github/workflows/ci.yaml` exécute `./gradlew --no-daemon clean test bootJar`.
- Publication Docker : `/.github/workflows/docker-publish.yaml` publie sur GHCR après réussite de la CI.

## Structure du repo (racine)
- `build.gradle.kts`, `settings.gradle.kts` : build Gradle
- `gradlew`, `gradlew.bat`, `gradle/` : Gradle wrapper
- `pom.xml`, `mvnw*`, `.mvn/` : historique Maven (à supprimer une fois la migration stabilisée)
- `Dockerfile` : build multi-stage + runtime JRE Alpine
- `compose.yaml` : lancement du service
- `compose.db.yaml` : override Postgres (profil `db`)
- `data/` : fichiers JSON utilisés en runtime