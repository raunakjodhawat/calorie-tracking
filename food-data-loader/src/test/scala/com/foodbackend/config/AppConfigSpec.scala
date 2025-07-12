package com.foodbackend.config

import zio._
import zio.test._
import zio.test.Assertion._
import zio.config._
import zio.config.magnolia._
import zio.config.typesafe._

object AppConfigSpec extends ZIOSpecDefault {

  def spec = suite("AppConfigSpec")(
    suite("AppConfig")(
      test("should load configuration from HOCON") {
        val configString = """
          app {
            database {
              host = "localhost"
              port = 5432
              database = "test_db"
              username = "test_user"
              password = "test_pass"
            }
            foodDataPath = "test-food-data.json"
          }
        """

        val config = TypesafeConfigProvider
          .fromHoconString(configString)
          .load(deriveConfig[AppConfig].nested("app"))

        for {
          appConfig <- config
        } yield assertTrue(appConfig.database.host == "localhost") &&
               assertTrue(appConfig.database.port == 5432) &&
               assertTrue(appConfig.database.database == "test_db") &&
               assertTrue(appConfig.database.username == "test_user") &&
               assertTrue(appConfig.database.password == "test_pass") &&
               assertTrue(appConfig.foodDataPath == "test-food-data.json")
      },

      test("should handle missing optional fields with defaults") {
        val configString = """
          app {
            database {
              host = "localhost"
              port = 5432
              database = "test_db"
              username = "test_user"
              password = "test_pass"
            }
          }
        """

        val config = TypesafeConfigProvider
          .fromHoconString(configString)
          .load(deriveConfig[AppConfig].nested("app"))

        for {
          appConfig <- config
        } yield assertTrue(appConfig.database.host == "localhost") &&
               assertTrue(appConfig.database.port == 5432) &&
               assertTrue(appConfig.database.database == "test_db") &&
               assertTrue(appConfig.database.username == "test_user") &&
               assertTrue(appConfig.database.password == "test_pass")
      },

      test("should fail to load invalid configuration") {
        val configString = """
          app {
            database {
              host = "localhost"
              port = "invalid_port"
              database = "test_db"
              username = "test_user"
              password = "test_pass"
            }
            foodDataPath = "test-food-data.json"
          }
        """

        val config = TypesafeConfigProvider
          .fromHoconString(configString)
          .load(deriveConfig[AppConfig].nested("app"))

        for {
          result <- config.either
        } yield assertTrue(result.isLeft)
      },

      test("should handle different database configurations") {
        val configString = """
          app {
            database {
              host = "postgres.example.com"
              port = 5433
              database = "production_db"
              username = "prod_user"
              password = "prod_pass"
            }
            foodDataPath = "/data/food-data.json"
          }
        """

        val config = TypesafeConfigProvider
          .fromHoconString(configString)
          .load(deriveConfig[AppConfig].nested("app"))

        for {
          appConfig <- config
        } yield assertTrue(appConfig.database.host == "postgres.example.com") &&
               assertTrue(appConfig.database.port == 5433) &&
               assertTrue(appConfig.database.database == "production_db") &&
               assertTrue(appConfig.database.username == "prod_user") &&
               assertTrue(appConfig.database.password == "prod_pass") &&
               assertTrue(appConfig.foodDataPath == "/data/food-data.json")
      }
    )
  )
} 