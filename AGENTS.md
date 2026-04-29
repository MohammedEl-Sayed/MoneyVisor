# AGENTS.md — MoneyVisor Project Manifest

## Project Overview
- **Name**: MoneyVisor
- **Type**: Android App (Multi-module)
- **Tech Stack**: Kotlin, Jetpack Compose, Material 3, Room, Hilt, Navigation Compose
- **Target SDK**: 35
- **Min SDK**: 29

## Project Structure
- `:app`: Main entry point, navigation, and DI setup.
- `:core:designsystem`: Material 3 theme, icons, and animation constants.
- `:core:database`: Room database, entities, and DAOs.
- `:domain`: Business models and repository interfaces.
- `:data`: Repository implementation and data mapping.
- `:feature:dashboard`: Dashboard screen with financial summary and recent transactions.
- `:feature:transaction`: Add/Edit transaction screen.

## Key Design Patterns
- **MVVM**: Used for UI-Logic separation.
- **Reactive State**: Transactions and totals are observed via `StateFlow`.
- **Clean Architecture**: Domain layer is isolated from implementation details.

## Commands
- `./gradlew build` — Build the project
- `./gradlew test` — Run unit tests

## Important Rules
- Use `MoneyVisorAnimations.smoothSpring` for all standard UI animations.
- Icons should use `MoneyVisorIcons` to maintain consistency.
- Ensure RTL compatibility for Arabic ('ar') locale.
