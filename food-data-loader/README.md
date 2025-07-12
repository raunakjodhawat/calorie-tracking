# food-data-loader

A Scala/ZIO application for loading food data into a Postgres database. It parses food data from a JSON file and stores it in a normalized schema, including foods, portions, and macronutrient conversion factors.

## Features
- Loads food data from a JSON file (USDA Foundation Foods format)
- Stores foods, portions, and nutrient conversion factors in Postgres
- Written in Scala with ZIO for effect and resource management
- Uses plain JDBC (no Quill)
- Includes a full test suite (runs on H2 for fast CI)
- 100% test coverage (enforced by Scoverage)

## Running Locally

1. Start Postgres (see docker-compose below for config)
2. Set DB config in `src/main/resources/application.conf` or via env vars
3. Run:
   ```sh
   sbt run
   ```

## Running Tests

```sh
sbt test
sbt coverage test coverageReport
```

## Docker

To run the app and Postgres together:

```sh
docker-compose up --build
```

This will build the app, start Postgres, and run the loader.

To run tests in Docker:

```sh
docker-compose run --rm food-data-loader sbt test
```

## CI

- GitHub Actions workflow runs tests and coverage in Docker
- Coverage is uploaded to Codecov

## Configuration

- DB connection info is read from environment variables or `application.conf`
- See `docker-compose.yml` for example DB settings

## License

MIT 