package com.fooddataLoader.loader

import com.fooddataLoader.domain._
import com.fooddataLoader.config.AppConfig
import com.fooddataLoader.repository.FoodRepository
import zio._
import zio.test._
import zio.test.Assertion._
import zio.json._

object FoodDataLoaderSpec extends ZIOSpecDefault {

  def spec = suite("FoodDataLoaderSpec")(
    suite("FoodDataLoader")(
      test("should parse valid JSON structure") {
        val jsonContent = """
          {
            "FoundationFoods": [
              {
                "fdcId": 123456,
                "description": "Test Food 1",
                "foodPortions": [
                  {
                    "id": 1,
                    "value": 1.0,
                    "measureUnit": {
                      "id": 1000,
                      "name": "cup",
                      "abbreviation": "cup"
                    },
                    "modifier": "sliced",
                    "gramWeight": 100.0,
                    "sequenceNumber": 1,
                    "amount": 1.0,
                    "minYearAcquired": 2020
                  }
                ],
                "nutrientConversionFactors": [
                  {
                    "type": ".CalorieConversionFactor",
                    "proteinValue": 3.0,
                    "fatValue": 8.0,
                    "carbohydrateValue": 4.0,
                    "value": 15.0
                  }
                ]
              },
              {
                "fdcId": 789012,
                "description": "Test Food 2",
                "foodPortions": [],
                "nutrientConversionFactors": []
              }
            ]
          }
        """

        val result = jsonContent.fromJson[Map[String, List[Food]]]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get.contains("FoundationFoods")) &&
        assertTrue(result.toOption.get("FoundationFoods").length == 2) &&
        assertTrue(result.toOption.get("FoundationFoods").head.fdcId == 123456) &&
        assertTrue(result.toOption.get("FoundationFoods").head.description == "Test Food 1") &&
        assertTrue(result.toOption.get("FoundationFoods").head.foodPortions.isDefined) &&
        assertTrue(result.toOption.get("FoundationFoods").head.foodPortions.get.length == 1) &&
        assertTrue(result.toOption.get("FoundationFoods").head.nutrientConversionFactors.isDefined) &&
        assertTrue(result.toOption.get("FoundationFoods").head.nutrientConversionFactors.get.length == 1) &&
        assertTrue(result.toOption.get("FoundationFoods")(1).fdcId == 789012) &&
        assertTrue(result.toOption.get("FoundationFoods")(1).description == "Test Food 2") &&
        assertTrue(result.toOption.get("FoundationFoods")(1).foodPortions.get.isEmpty) &&
        assertTrue(result.toOption.get("FoundationFoods")(1).nutrientConversionFactors.get.isEmpty)
      },

      test("should handle JSON with missing FoundationFoods key") {
        val jsonContent = """
          {
            "OtherKey": []
          }
        """

        val result = jsonContent.fromJson[Map[String, List[Food]]]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get.getOrElse("FoundationFoods", List.empty).isEmpty)
      },

      test("should handle empty FoundationFoods array") {
        val jsonContent = """
          {
            "FoundationFoods": []
          }
        """

        val result = jsonContent.fromJson[Map[String, List[Food]]]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get("FoundationFoods").isEmpty)
      },

      test("should handle food with missing optional fields") {
        val jsonContent = """
          {
            "FoundationFoods": [
              {
                "fdcId": 123456,
                "description": "Test Food"
              }
            ]
          }
        """

        val result = jsonContent.fromJson[Map[String, List[Food]]]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get("FoundationFoods").length == 1) &&
        assertTrue(result.toOption.get("FoundationFoods").head.fdcId == 123456) &&
        assertTrue(result.toOption.get("FoundationFoods").head.description == "Test Food") &&
        assertTrue(result.toOption.get("FoundationFoods").head.foodPortions.isEmpty) &&
        assertTrue(result.toOption.get("FoundationFoods").head.nutrientConversionFactors.isEmpty)
      },

      test("should fail to parse invalid JSON") {
        val jsonContent = """
          {
            "FoundationFoods": [
              {
                "fdcId": "invalid",
                "description": "Test Food"
              }
            ]
          }
        """

        val result = jsonContent.fromJson[Map[String, List[Food]]]
        
        assertTrue(result.isLeft)
      },

      test("should handle food with null optional fields") {
        val jsonContent = """
          {
            "FoundationFoods": [
              {
                "fdcId": 123456,
                "description": "Test Food",
                "foodPortions": null,
                "nutrientConversionFactors": null
              }
            ]
          }
        """

        val result = jsonContent.fromJson[Map[String, List[Food]]]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get("FoundationFoods").length == 1) &&
        assertTrue(result.toOption.get("FoundationFoods").head.foodPortions.isEmpty) &&
        assertTrue(result.toOption.get("FoundationFoods").head.nutrientConversionFactors.isEmpty)
      }
    )
  ).provide(
    // No layers needed for JSON parsing tests
  )

  // MockFoodRepository class removed as it's no longer needed
} 