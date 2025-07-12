package com.foodbackend

import com.foodbackend.domain._
import zio.test._
import zio.json._

object ModelsSpec extends ZIOSpecDefault {

  def spec = suite("ModelsSpec")(
    suite("Food model")(
      test("should parse food JSON correctly with all fields") {
        val json = """
          {
            "fdcId": 325524,
            "description": "Sunflower seeds, dry roasted, salted",
            "foodPortions": [
              {
                "id": 119299,
                "value": 1.0,
                "measureUnit": {
                  "id": 1000,
                  "name": "cup",
                  "abbreviation": "cup"
                },
                "modifier": "",
                "gramWeight": 127.0,
                "sequenceNumber": 1,
                "amount": 1.0,
                "minYearAcquired": 2015
              }
            ],
            "nutrientConversionFactors": [
              {
                "type": ".CalorieConversionFactor",
                "proteinValue": 3.47,
                "fatValue": 8.37,
                "carbohydrateValue": 4.07,
                "value": 15.91
              }
            ]
          }
        """

        val result = json.fromJson[Food]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get.fdcId == 325524) &&
        assertTrue(result.toOption.get.description == "Sunflower seeds, dry roasted, salted") &&
        assertTrue(result.toOption.get.foodPortions.isDefined) &&
        assertTrue(result.toOption.get.foodPortions.get.length == 1) &&
        assertTrue(result.toOption.get.nutrientConversionFactors.isDefined) &&
        assertTrue(result.toOption.get.nutrientConversionFactors.get.length == 1)
      },
      
      test("should parse food JSON with missing optional fields") {
        val json = """
          {
            "fdcId": 123456,
            "description": "Test food"
          }
        """

        val result = json.fromJson[Food]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get.fdcId == 123456) &&
        assertTrue(result.toOption.get.description == "Test food") &&
        assertTrue(result.toOption.get.foodPortions.isEmpty) &&
        assertTrue(result.toOption.get.nutrientConversionFactors.isEmpty)
      },
      
      test("should fail to parse invalid JSON") {
        val json = """
          {
            "fdcId": "invalid",
            "description": "Test food"
          }
        """

        val result = json.fromJson[Food]
        
        assertTrue(result.isLeft)
      }
    ),
    
    suite("FoodPortion model")(
      test("should parse food portion JSON correctly") {
        val json = """
          {
            "id": 119299,
            "value": 1.0,
            "measureUnit": {
              "id": 1000,
              "name": "cup",
              "abbreviation": "cup"
            },
            "modifier": "sliced",
            "gramWeight": 127.0,
            "sequenceNumber": 1,
            "amount": 1.0,
            "minYearAcquired": 2015
          }
        """

        val result = json.fromJson[FoodPortion]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get.id == 119299) &&
        assertTrue(result.toOption.get.value == 1.0) &&
        assertTrue(result.toOption.get.measureUnit.id == 1000) &&
        assertTrue(result.toOption.get.measureUnit.name == "cup") &&
        assertTrue(result.toOption.get.modifier.contains("sliced")) &&
        assertTrue(result.toOption.get.gramWeight == 127.0) &&
        assertTrue(result.toOption.get.sequenceNumber == 1) &&
        assertTrue(result.toOption.get.amount == 1.0) &&
        assertTrue(result.toOption.get.minYearAcquired.contains(2015))
      },
      
      test("should parse food portion with null modifier and minYearAcquired") {
        val json = """
          {
            "id": 119299,
            "value": 1.0,
            "measureUnit": {
              "id": 1000,
              "name": "cup",
              "abbreviation": "cup"
            },
            "modifier": null,
            "gramWeight": 127.0,
            "sequenceNumber": 1,
            "amount": 1.0,
            "minYearAcquired": null
          }
        """

        val result = json.fromJson[FoodPortion]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get.modifier.isEmpty) &&
        assertTrue(result.toOption.get.minYearAcquired.isEmpty)
      }
    ),
    
    suite("MeasureUnit model")(
      test("should parse measure unit JSON correctly") {
        val json = """
          {
            "id": 1000,
            "name": "cup",
            "abbreviation": "cup"
          }
        """

        val result = json.fromJson[MeasureUnit]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get.id == 1000) &&
        assertTrue(result.toOption.get.name == "cup") &&
        assertTrue(result.toOption.get.abbreviation == "cup")
      }
    ),
    
    suite("NutrientConversionFactor model")(
      test("should parse nutrient conversion factor with all values") {
        val json = """
          {
            "type": ".CalorieConversionFactor",
            "proteinValue": 3.47,
            "fatValue": 8.37,
            "carbohydrateValue": 4.07,
            "value": 15.91
          }
        """

        val result = json.fromJson[NutrientConversionFactor]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get.`type` == ".CalorieConversionFactor") &&
        assertTrue(result.toOption.get.proteinValue.contains(3.47)) &&
        assertTrue(result.toOption.get.fatValue.contains(8.37)) &&
        assertTrue(result.toOption.get.carbohydrateValue.contains(4.07)) &&
        assertTrue(result.toOption.get.value.contains(15.91))
      },
      
      test("should parse nutrient conversion factor with null values") {
        val json = """
          {
            "type": ".CalorieConversionFactor",
            "proteinValue": null,
            "fatValue": null,
            "carbohydrateValue": null,
            "value": null
          }
        """

        val result = json.fromJson[NutrientConversionFactor]
        
        assertTrue(result.isRight) &&
        assertTrue(result.toOption.get.`type` == ".CalorieConversionFactor") &&
        assertTrue(result.toOption.get.proteinValue.isEmpty) &&
        assertTrue(result.toOption.get.fatValue.isEmpty) &&
        assertTrue(result.toOption.get.carbohydrateValue.isEmpty) &&
        assertTrue(result.toOption.get.value.isEmpty)
      }
    ),
    
    suite("JSON encoding")(
      test("should encode and decode Food correctly") {
        val food = Food(
          fdcId = 123456,
          description = "Test food",
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
            )
          )),
          nutrientConversionFactors = Some(List(
            NutrientConversionFactor(
              `type` = ".CalorieConversionFactor",
              proteinValue = Some(3.0),
              fatValue = Some(8.0),
              carbohydrateValue = Some(4.0),
              value = Some(15.0)
            )
          ))
        )

        val encoded = food.toJson
        val decoded = encoded.fromJson[Food]
        
        assertTrue(decoded.isRight) &&
        assertTrue(decoded.toOption.get == food)
      }
    )
  )
} 