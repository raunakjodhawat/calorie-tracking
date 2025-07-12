# Food Backend

A Scala + ZIO backend application that loads food data from a JSON file into PostgreSQL database.

## Features

- **ZIO-based architecture** for functional programming and error handling
- **PostgreSQL integration** using Quill for type-safe database operations
- **JSON data loading** from external files
- **Configuration management** using ZIO Config
- **Structured logging** with ZIO Logging

## Prerequisites

- Scala 2.13.12
- SBT
- PostgreSQL database
- Java 8 or higher

## Setup

### 1. Database Setup

First, create a PostgreSQL database:

```sql
CREATE DATABASE food_db;
```

### 2. Configuration

Update the database configuration in `src/main/resources/application.conf`:

```hocon
app {
  database {
    host = "localhost"
    port = 5432
    database = "food_db"
    username = "postgres"
    password = "postgres"
  }
  foodDataPath = "/path/to/your/food-data.json"
}
```

### 3. Build and Run

```bash
# Compile the project
sbt compile

# Run the application
sbt run
```

## Project Structure

```
src/main/scala/com/foodbackend/
├── config/
│   └── AppConfig.scala          # Configuration management
├── domain/
│   └── Models.scala             # Domain models and JSON codecs
├── repository/
│   ├── FoodRepository.scala     # Database operations
│   └── DatabaseSetup.scala      # Database schema setup
├── loader/
│   └── FoodDataLoader.scala     # JSON data loading service
└── Main.scala                   # Application entry point
```

## Database Schema

The application creates three main tables:

### `foods`
- `fdc_id` (BIGINT, PRIMARY KEY): Food Data Central ID
- `description` (TEXT): Food description
- `food_class` (VARCHAR): Food classification
- `data_type` (VARCHAR): Data type
- `ndb_number` (BIGINT): NDB number
- `publication_date` (VARCHAR): Publication date
- `category_id` (BIGINT): Category ID
- `category_code` (VARCHAR): Category code
- `category_description` (TEXT): Category description
- `is_historical_reference` (BOOLEAN): Historical reference flag
- `created_at` (TIMESTAMP): Creation timestamp

### `food_nutrients`
- `id` (BIGINT, PRIMARY KEY): Nutrient record ID
- `food_fdc_id` (BIGINT, FOREIGN KEY): Reference to foods table
- `nutrient_id` (BIGINT): Nutrient ID
- `nutrient_number` (VARCHAR): Nutrient number
- `nutrient_name` (TEXT): Nutrient name
- `nutrient_rank` (INTEGER): Nutrient rank
- `nutrient_unit_name` (VARCHAR): Nutrient unit
- `amount` (DOUBLE PRECISION): Nutrient amount
- `data_points` (INTEGER): Number of data points
- `derivation_code` (VARCHAR): Derivation code
- `derivation_description` (TEXT): Derivation description
- `source_id` (BIGINT): Source ID
- `source_code` (VARCHAR): Source code
- `source_description` (TEXT): Source description
- `max` (DOUBLE PRECISION): Maximum value
- `min` (DOUBLE PRECISION): Minimum value
- `median` (DOUBLE PRECISION): Median value

### `food_portions`
- `id` (BIGINT, PRIMARY KEY): Portion record ID
- `food_fdc_id` (BIGINT, FOREIGN KEY): Reference to foods table
- `value` (DOUBLE PRECISION): Portion value
- `measure_unit_id` (BIGINT): Measure unit ID
- `measure_unit_name` (VARCHAR): Measure unit name
- `measure_unit_abbreviation` (VARCHAR): Measure unit abbreviation
- `modifier` (VARCHAR): Portion modifier
- `gram_weight` (DOUBLE PRECISION): Gram weight
- `sequence_number` (INTEGER): Sequence number
- `amount` (DOUBLE PRECISION): Amount
- `min_year_acquired` (INTEGER): Minimum year acquired

## Usage

The application will:

1. Create database tables if they don't exist
2. Load food data from the specified JSON file
3. Parse the JSON and extract food information
4. Store the data in the PostgreSQL database
5. Log the progress and any errors

## Dependencies

- **ZIO**: Functional programming library
- **ZIO JSON**: JSON parsing and serialization
- **ZIO Config**: Configuration management
- **ZIO Logging**: Structured logging
- **Quill**: Type-safe database queries
- **PostgreSQL**: Database driver

## Error Handling

The application uses ZIO's error handling capabilities to:
- Handle database connection errors
- Validate JSON data
- Log errors with context
- Provide meaningful error messages

## Performance

- Uses batch inserts for better performance
- Processes data in parallel where possible
- Includes progress logging for monitoring
- Limits initial load to first 10 records for testing 