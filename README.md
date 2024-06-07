## FHIR Facade on Postgres

A simple FHIR facade utilising HAPI FHIR, on top of a Postgres database.

Included FHIR resources are: `Patient`, `Observation`, with read (eg. `Observation/{id}`) and search (eg. `/Observation`).
Included in the sample data are 12 blood pressure observations and 12 heart rate observations. These are differentiated on the `Observation` endpoint as:

- `Observation/1-blood-pressure`
- `Observation/1-heart-rate`

# Prerequisites

Java 17, Maven, PostgreSQL or Docker, PSQL or DBMS (to seed database)

# Local setup

1. Create a local Postgres instance in Docker by running: `docker-compose up`
2. Seed the database with the included sample data (`scripts/seed_database.sql`), using PSQL or a database management system such as DBeaver or PGAdmin
3. Run the app from an IDE (ie. IntelliJ or VS Code)
4. Make requests against localhost eg. http://localhost:8080/Patient/

The app port (default `8080`) and database connection details can be configured within `application.properties`;
