# RitaMPC

Short README to get started. This project is structured **by domain first**, and **within each domain it is organized “package by layer”** (e.g., controller/service/repository/model), similar to the idea discussed here:  
<https://medium.com/@akintopbas96/spring-boot-code-structure-package-by-layer-vs-package-by-feature-5331a0c911fe>

---

## Project Structure (Domain → Package by Layer)

Source root:

- `src/main/java/lu/feschhaff/ritampc/`

Domains (top-level packages):

- `CommonTools/`  
  Shared utilities/helpers used across domains.

- `HomeAssistant/`  
  Home Assistant integration domain.

- `ModelPredictiveController/`  
  MPC (control logic) domain.

- `PredictionModel/`  
  Prediction / forecasting domain.

Application entry point:

- `RitaMpcApplication/`  
  Spring Boot application bootstrap (main app wiring / startup).

Resources:

- `src/main/resources/`
    - `application.properties` (configuration)
    - `static/` (static assets)
    - `templates/` (server-rendered templates, if used)
    - `banner.txt`

---

## Conventions

Inside each **domain package**, prefer **package-by-layer** to keep responsibilities clear, for example:

- `.../<Domain>/controller`
- `.../<Domain>/service`
- `.../<Domain>/repository`
- `.../<Domain>/model` (or `dto`, `entity`, etc.)

This keeps the codebase consistent while still grouping by business feature/domain first.

---

## Build / Run (placeholder)

- Build: `./mvnw clean package`
- Run: `./mvnw spring-boot:run`

(Adjust once the project setup stabilizes.)

---

## Notes / TODO

- Expand each domain section with purpose, key flows, and module boundaries.
- Document API endpoints and configuration keys as they solidify.