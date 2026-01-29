# Contexte rapide — kolok

## Ce que fait le projet
**kolok** est une application Spring Boot en Kotlin orientée planification/rotations de responsabilités, avec des adaptateurs d’entrée (Discord/REST/scheduler).

## Points d’entrée
- Main : `src/main/kotlin/cat/ohmushi/kolok/planning/KolokApplication.kt`
- Wiring : `src/main/kotlin/.../bootstrap/Wiring.kt`

## Arborescence (résumé)
- `domain/` : modèle métier (Period, Responsibility, rotations, disponibilités, événements, exceptions)
- `application/` : ports + services (use cases)
- `adapters/in/` : contrôleurs REST, listeners Discord, scheduler
- `adapters/infrastructure/` : implémentations techniques (DB, repositories, clients)

## Runtime & config
- Config Spring via `src/main/resources/application.properties` + variables d’environnement.
- Données d’exemple : `data/catalog.json`, `data/plannings.json`.

## Docker
- Image publiée sur GHCR et déployée via Watchtower.
- `compose.yaml` + éventuellement `compose.db.yaml`.

## Bonnes pratiques attendues
- Tests unitaires/integ dans `src/test/kotlin`.
- Domain pur (pas d’annotations Spring dans `domain`).
