package com.fooddataLoader.loader

import com.fooddataLoader.config.AppConfig
import com.fooddataLoader.domain._
import com.fooddataLoader.repository.FoodRepository
import zio._
import zio.json._
import zio.stream._

import java.io.File
import scala.io.Source

trait FoodDataLoader {
  def loadFoodData: ZIO[Scope, Throwable, Unit]
  def loadFoodDataFromFile(filePath: String): ZIO[Scope, Throwable, Unit]
}

class FoodDataLoaderLive(
  config: AppConfig,
  foodRepository: FoodRepository
) extends FoodDataLoader {

  override def loadFoodData: ZIO[Scope, Throwable, Unit] = {
    loadFoodDataFromFile(config.foodDataPath)
  }

  override def loadFoodDataFromFile(filePath: String): ZIO[Scope, Throwable, Unit] = {
    for {
      _ <- ZIO.logInfo(s"Starting to load food data from: $filePath")
      content <- ZIO.attempt {
        val resourceStream = getClass.getClassLoader.getResourceAsStream(filePath)
        if (resourceStream == null) {
          throw new RuntimeException(s"Resource not found in classpath: $filePath")
        }
        scala.io.Source.fromInputStream(resourceStream).mkString
      }
      // Parse the JSON structure: {"FoundationFoods": [...]}
      jsonData <- ZIO.fromEither(content.fromJson[Map[String, List[Food]]])
        .mapError(e => new RuntimeException(s"Failed to parse JSON: $e"))
      foods = jsonData.getOrElse("FoundationFoods", List.empty)
      _ <- ZIO.logInfo(s"Parsed ${foods.length} foods from JSON")
      // Sequential processing to avoid connection pool exhaustion (change to parallel when ZIO is upgraded)
      _ <- ZIO.foreach(foods) { food => // Process all foods
        ZIO.scoped {
          for {
            _ <- foodRepository.insertFood(food)
            _ <- foodRepository.insertFoodPortions(food.fdcId, food.foodPortions.getOrElse(Nil))
            _ <- foodRepository.insertNutrientConversionFactors(food.fdcId, food.nutrientConversionFactors.getOrElse(Nil))
            _ <- ZIO.logInfo(s"Loaded food: ${food.description} (FDC ID: ${food.fdcId})")
          } yield ()
        }
      }
      _ <- ZIO.logInfo("Food data loading completed successfully")
    } yield ()
  }
}

object FoodDataLoader {
  def loadFoodData: ZIO[FoodDataLoader & Scope, Throwable, Unit] =
    ZIO.serviceWithZIO[FoodDataLoader](_.loadFoodData)

  def loadFoodDataFromFile(filePath: String): ZIO[FoodDataLoader & Scope, Throwable, Unit] =
    ZIO.serviceWithZIO[FoodDataLoader](_.loadFoodDataFromFile(filePath))

  val live: ZLayer[AppConfig & FoodRepository, Nothing, FoodDataLoader] =
    ZLayer {
      for {
        config <- ZIO.service[AppConfig]
        foodRepository <- ZIO.service[FoodRepository]
      } yield new FoodDataLoaderLive(config, foodRepository)
    }
} 