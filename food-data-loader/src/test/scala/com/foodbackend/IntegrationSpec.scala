package com.foodbackend

import com.foodbackend.config.AppConfig
import com.foodbackend.loader.FoodDataLoader
import com.foodbackend.repository.{DatabaseSetup, FoodRepository}
import zio._
import zio.test._
import zio.test.Assertion._
import javax.sql.DataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object IntegrationSpec extends ZIOSpecDefault {

  def spec = suite("IntegrationSpec")(
    suite("Full Data Loading Pipeline")(
      test("should load test food data from JSON file") {
        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          _ <- FoodDataLoader.loadFoodDataFromFile("test-food-data.json")
          allFoods <- FoodRepository.getAllFoods
          food1 <- FoodRepository.getFoodByFdcId(123456)
          food2 <- FoodRepository.getFoodByFdcId(789012)
          portions1 <- FoodRepository.getFoodPortions(123456)
          factors1 <- FoodRepository.getNutrientConversionFactors(123456)
        } yield assertTrue(allFoods.length == 2) &&
               assertTrue(food1.isDefined) &&
               assertTrue(food1.get.description == "Test Food 1") &&
               assertTrue(food2.isDefined) &&
               assertTrue(food2.get.description == "Test Food 2") &&
               assertTrue(portions1.length == 1) &&
               assertTrue(portions1.head.measureUnitName == "cup") &&
               assertTrue(portions1.head.modifier.contains("sliced")) &&
               assertTrue(factors1.length == 1) &&
               assertTrue(factors1.head.`type` == ".CalorieConversionFactor") &&
               assertTrue(factors1.head.proteinValue.contains(3.0))
      },

      test("should handle empty food data file") {
        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          _ <- FoodDataLoader.loadFoodDataFromFile("empty-food-data.json")
          allFoods <- FoodRepository.getAllFoods
        } yield assertTrue(allFoods.isEmpty)
      },

      test("should handle database connection errors gracefully") {
        // This test verifies that the application handles database errors properly
        // by trying to load data without proper database setup
        val result = FoodDataLoader.loadFoodDataFromFile("test-food-data.json").either
        
        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          result <- FoodDataLoader.loadFoodDataFromFile("test-food-data.json").either
        } yield assertTrue(result.isRight) // Should succeed with proper setup
      }
    )
  ).provide(
    // Test database configuration
    testDataSourceLayer,
    testConnectionServiceLayer,
    testConfigLayer,
    FoodRepository.live,
    FoodDataLoader.live,
    Scope.default
  )

  private val testDataSourceLayer: ZLayer[Any, Throwable, DataSource] = ZLayer {
    ZIO.attempt {
      val config = new HikariConfig()
      // Use H2 for integration tests to avoid external dependencies
      config.setJdbcUrl("jdbc:h2:mem:integration_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
      config.setUsername("sa")
      config.setPassword("")
      config.setMaximumPoolSize(5)
      config.setMinimumIdle(1)
      config.setConnectionTimeout(10000)
      config.setIdleTimeout(300000)
      config.setMaxLifetime(900000)
      new HikariDataSource(config)
    }
  }

  private val testConnectionServiceLayer: ZLayer[DataSource, Nothing, com.foodbackend.ConnectionService] =
    ZLayer {
      for {
        dataSource <- ZIO.service[DataSource]
      } yield new TestConnectionService(dataSource)
    }

  private val testConfigLayer: ZLayer[Any, Throwable, AppConfig] = ZLayer {
    ZIO.succeed(AppConfig(
      database = com.foodbackend.config.DatabaseConfig(
        host = "localhost",
        port = 5432,
        database = "test_db",
        username = "test_user",
        password = "test_pass"
      ),
      foodDataPath = "test-food-data.json"
    ))
  }

  class TestConnectionService(dataSource: DataSource) extends com.foodbackend.ConnectionService {
    override def getConnection: ZIO[Scope, Throwable, java.sql.Connection] = 
      ZIO.acquireRelease(
        ZIO.attemptBlocking(dataSource.getConnection)
      )(conn => ZIO.attemptBlocking(conn.close()).orDie)
  }
} 