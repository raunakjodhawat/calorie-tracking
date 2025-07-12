package com.foodbackend.repository

import com.foodbackend.domain._
import zio._
import zio.test._
import zio.test.Assertion._
import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.LocalDateTime
import javax.sql.DataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object FoodRepositorySpec extends ZIOSpecDefault {

  def spec = suite("FoodRepositorySpec")(
    suite("FoodRepository")(
      test("should insert and retrieve food") {
        val testFood = Food(
          fdcId = 123456,
          description = "Test Food",
          foodPortions = None,
          nutrientConversionFactors = None
        )

        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          _ <- FoodRepository.insertFood(testFood)
          retrieved <- FoodRepository.getFoodByFdcId(123456)
        } yield assertTrue(retrieved.isDefined) &&
               assertTrue(retrieved.get.fdcId == 123456) &&
               assertTrue(retrieved.get.description == "Test Food")
      },

      test("should insert food with portions and retrieve them") {
        val testFood = Food(
          fdcId = 789012,
          description = "Test Food with Portions",
          foodPortions = Some(List(
            FoodPortion(
              id = 1,
              value = 1.0,
              measureUnit = MeasureUnit(1, "cup", "cup"),
              modifier = Some("sliced"),
              gramWeight = 100.0,
              sequenceNumber = 1,
              amount = 1.0,
              minYearAcquired = Some(2020)
            ),
            FoodPortion(
              id = 2,
              value = 2.0,
              measureUnit = MeasureUnit(2, "tablespoon", "tbsp"),
              modifier = None,
              gramWeight = 30.0,
              sequenceNumber = 2,
              amount = 2.0,
              minYearAcquired = None
            )
          )),
          nutrientConversionFactors = None
        )

        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          _ <- FoodRepository.insertFood(testFood)
          _ <- FoodRepository.insertFoodPortions(testFood.fdcId, testFood.foodPortions.get)
          portions <- FoodRepository.getFoodPortions(testFood.fdcId)
        } yield assertTrue(portions.length == 2) &&
               assertTrue(portions.head.id == 1) &&
               assertTrue(portions.head.value == 1.0) &&
               assertTrue(portions.head.measureUnitName == "cup") &&
               assertTrue(portions.head.modifier.contains("sliced")) &&
               assertTrue(portions.head.minYearAcquired.contains(2020)) &&
               assertTrue(portions(1).id == 2) &&
               assertTrue(portions(1).value == 2.0) &&
               assertTrue(portions(1).measureUnitName == "tablespoon") &&
               assertTrue(portions(1).modifier.isEmpty) &&
               assertTrue(portions(1).minYearAcquired.isEmpty)
      },

      test("should insert food with nutrient conversion factors and retrieve them") {
        val testFood = Food(
          fdcId = 345678,
          description = "Test Food with Nutrients",
          foodPortions = None,
          nutrientConversionFactors = Some(List(
            NutrientConversionFactor(
              `type` = ".CalorieConversionFactor",
              proteinValue = Some(3.0),
              fatValue = Some(8.0),
              carbohydrateValue = Some(4.0),
              value = Some(15.0)
            ),
            NutrientConversionFactor(
              `type` = ".ProteinConversionFactor",
              proteinValue = Some(25.0),
              fatValue = None,
              carbohydrateValue = None,
              value = Some(25.0)
            )
          ))
        )

        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          _ <- FoodRepository.insertFood(testFood)
          _ <- FoodRepository.insertNutrientConversionFactors(testFood.fdcId, testFood.nutrientConversionFactors.get)
          factors <- FoodRepository.getNutrientConversionFactors(testFood.fdcId)
        } yield assertTrue(factors.length == 2) &&
               assertTrue(factors.head.`type` == ".CalorieConversionFactor") &&
               assertTrue(factors.head.proteinValue.contains(3.0)) &&
               assertTrue(factors.head.fatValue.contains(8.0)) &&
               assertTrue(factors.head.carbohydrateValue.contains(4.0)) &&
               assertTrue(factors.head.value.contains(15.0)) &&
               assertTrue(factors(1).`type` == ".ProteinConversionFactor") &&
               assertTrue(factors(1).proteinValue.contains(25.0)) &&
               assertTrue(factors(1).fatValue.isEmpty) &&
               assertTrue(factors(1).carbohydrateValue.isEmpty) &&
               assertTrue(factors(1).value.contains(25.0))
      },

      test("should retrieve all foods") {
        val testFoods = List(
          Food(fdcId = 111111, description = "Food 1", foodPortions = None, nutrientConversionFactors = None),
          Food(fdcId = 222222, description = "Food 2", foodPortions = None, nutrientConversionFactors = None),
          Food(fdcId = 333333, description = "Food 3", foodPortions = None, nutrientConversionFactors = None)
        )

        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          _ <- ZIO.foreach(testFoods)(FoodRepository.insertFood)
          allFoods <- FoodRepository.getAllFoods
        } yield assertTrue(allFoods.length == 3) &&
               assertTrue(allFoods.map(_.fdcId).toSet == Set(111111L, 222222L, 333333L)) &&
               assertTrue(allFoods.map(_.description).toSet == Set("Food 1", "Food 2", "Food 3"))
      },

      test("should return None for non-existent food") {
        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          result <- FoodRepository.getFoodByFdcId(999999)
        } yield assertTrue(result.isEmpty)
      },

      test("should handle empty portions list") {
        val testFood = Food(
          fdcId = 456789,
          description = "Test Food",
          foodPortions = Some(List.empty),
          nutrientConversionFactors = None
        )

        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          _ <- FoodRepository.insertFood(testFood)
          _ <- FoodRepository.insertFoodPortions(testFood.fdcId, testFood.foodPortions.get)
          portions <- FoodRepository.getFoodPortions(testFood.fdcId)
        } yield assertTrue(portions.isEmpty)
      },

      test("should handle empty nutrient conversion factors list") {
        val testFood = Food(
          fdcId = 567890,
          description = "Test Food",
          foodPortions = None,
          nutrientConversionFactors = Some(List.empty)
        )

        for {
          _ <- DatabaseSetup.dropTables
          _ <- DatabaseSetup.createTables
          _ <- FoodRepository.insertFood(testFood)
          _ <- FoodRepository.insertNutrientConversionFactors(testFood.fdcId, testFood.nutrientConversionFactors.get)
          factors <- FoodRepository.getNutrientConversionFactors(testFood.fdcId)
        } yield assertTrue(factors.isEmpty)
      }
    )
  ).provide(
    // Test database configuration
    testDataSourceLayer,
    testConnectionServiceLayer,
    FoodRepository.live,
    Scope.default
  )

  private val testDataSourceLayer: ZLayer[Any, Throwable, DataSource] = ZLayer {
    ZIO.attempt {
      val config = new HikariConfig()
      config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
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

  class TestConnectionService(dataSource: DataSource) extends com.foodbackend.ConnectionService {
    override def getConnection: ZIO[Scope, Throwable, Connection] = 
      ZIO.acquireRelease(
        ZIO.attemptBlocking(dataSource.getConnection)
      )(conn => ZIO.attemptBlocking(conn.close()).orDie)
  }
} 