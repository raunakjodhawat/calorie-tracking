# Testing Guide

This document explains how to run tests for the Food Data Loader project.

## Test Coverage Requirements

The project requires **100% test coverage** for all statements and branches. This ensures code quality and reliability.

## Running Tests Locally

### Prerequisites

1. **Java 11** or higher
2. **SBT** (Scala Build Tool)
3. **Docker** and **Docker Compose** (for integration tests)

### Quick Start

1. **Start the test database:**
   ```bash
   docker-compose up -d postgres-test
   ```

2. **Run all tests:**
   ```bash
   sbt test
   ```

3. **Run tests with coverage:**
   ```bash
   sbt coverage test coverageReport
   ```

4. **View coverage report:**
   ```bash
   sbt coverageReport
   ```
   The report will be generated in `target/scala-2.13/scoverage-report/`

### Test Types

#### Unit Tests
- **ModelsSpec**: Tests JSON parsing and domain model validation
- **AppConfigSpec**: Tests configuration loading and validation
- **FoodDataLoaderSpec**: Tests JSON parsing and data loading logic

#### Integration Tests
- **IntegrationSpec**: Tests the full data loading pipeline with a real database
- **FoodRepositorySpec**: Tests database operations (currently using H2 in-memory)

### Test Database Setup

The project uses two database configurations:

1. **H2 In-Memory Database** (for unit tests)
   - No setup required
   - Fast execution
   - Isolated per test

2. **PostgreSQL via Docker** (for integration tests)
   - Uses Docker Compose
   - Port: 5433
   - Database: `food_test_db`
   - User: `test_user`
   - Password: `test_password`

### Running Specific Tests

```bash
# Run only unit tests
sbt "testOnly *Spec"

# Run only integration tests
sbt "testOnly *IntegrationSpec"

# Run tests with specific pattern
sbt "testOnly *ModelsSpec"

# Run tests with coverage
sbt "coverage test coverageReport"
```

## CI/CD Pipeline

### GitHub Actions

The project includes a GitHub Actions workflow (`.github/workflows/ci.yml`) that:

1. **Sets up PostgreSQL** service container
2. **Runs unit tests** with coverage
3. **Runs integration tests** with Docker Compose
4. **Uploads coverage reports** to Codecov
5. **Enforces 100% coverage** requirements

### Local CI Simulation

To simulate the CI environment locally:

```bash
# Start PostgreSQL container
docker-compose up -d postgres-test

# Wait for database to be ready
while ! pg_isready -h localhost -p 5433 -U test_user -d food_test_db; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done

# Run tests with environment variables
POSTGRES_HOST=localhost POSTGRES_PORT=5433 POSTGRES_DB=food_test_db POSTGRES_USER=test_user POSTGRES_PASSWORD=test_password sbt test
```

## Test Data

### Test Files

- `src/test/resources/test-food-data.json`: Sample food data for testing
- `src/test/resources/empty-food-data.json`: Empty data for edge case testing
- `src/test/resources/application.conf`: Test configuration

### Test Coverage Areas

1. **Domain Models** (100% coverage)
   - JSON parsing and validation
   - Optional field handling
   - Error cases

2. **Configuration** (100% coverage)
   - HOCON parsing
   - Default values
   - Validation errors

3. **Data Loading** (100% coverage)
   - JSON file reading
   - Data transformation
   - Error handling

4. **Database Operations** (100% coverage)
   - CRUD operations
   - Connection management
   - Batch operations

5. **Integration** (100% coverage)
   - End-to-end data loading
   - Database setup/teardown
   - Error scenarios

## Troubleshooting

### Common Issues

1. **Database Connection Errors**
   ```bash
   # Ensure PostgreSQL is running
   docker-compose ps
   docker-compose logs postgres-test
   ```

2. **Coverage Below 100%**
   - Check the coverage report for uncovered lines
   - Add tests for missing scenarios
   - Ensure all branches are tested

3. **Test Failures**
   ```bash
   # Clean and rebuild
   sbt clean test
   
   # Run with verbose output
   sbt "testOnly *Spec -- -v"
   ```

### Debugging Tests

```bash
# Run single test with debug output
sbt "testOnly *ModelsSpec -- -t \"should parse food JSON correctly\""

# Run with detailed logging
sbt "testOnly *Spec -- -l DEBUG"
```

## Best Practices

1. **Write Tests First**: Follow TDD when possible
2. **Test Edge Cases**: Include null values, empty lists, invalid data
3. **Isolate Tests**: Each test should be independent
4. **Use Descriptive Names**: Test names should explain the scenario
5. **Mock External Dependencies**: Use mocks for external services
6. **Test Error Conditions**: Ensure error handling works correctly

## Coverage Reports

After running tests with coverage, view the report:

```bash
# Generate HTML report
sbt coverageReport

# Open in browser (macOS)
open target/scala-2.13/scoverage-report/index.html

# Open in browser (Linux)
xdg-open target/scala-2.13/scoverage-report/index.html
```

The coverage report shows:
- **Statement Coverage**: Percentage of code lines executed
- **Branch Coverage**: Percentage of conditional branches tested
- **File-by-file breakdown**: Detailed coverage per file
- **Missing coverage**: Lines and branches not tested 