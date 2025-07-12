package com.fooddataLoader.domain

import zio.json._

case class Food(
  fdcId: Long,
  description: String,
  foodPortions: Option[List[FoodPortion]],
  nutrientConversionFactors: Option[List[NutrientConversionFactor]]
)

case class FoodPortion(
  id: Long,
  value: Double,
  measureUnit: MeasureUnit,
  modifier: Option[String],
  gramWeight: Double,
  sequenceNumber: Int,
  amount: Double,
  minYearAcquired: Option[Int]
)

case class MeasureUnit(
  id: Long,
  name: String,
  abbreviation: String
)

case class NutrientConversionFactor(
  `type`: String,
  proteinValue: Option[Double],
  fatValue: Option[Double],
  carbohydrateValue: Option[Double],
  value: Option[Double]
)

object Food {
  implicit val decoder: JsonDecoder[Food] = DeriveJsonDecoder.gen[Food]
  implicit val encoder: JsonEncoder[Food] = DeriveJsonEncoder.gen[Food]
}

object FoodPortion {
  implicit val decoder: JsonDecoder[FoodPortion] = DeriveJsonDecoder.gen[FoodPortion]
  implicit val encoder: JsonEncoder[FoodPortion] = DeriveJsonEncoder.gen[FoodPortion]
}

object MeasureUnit {
  implicit val decoder: JsonDecoder[MeasureUnit] = DeriveJsonDecoder.gen[MeasureUnit]
  implicit val encoder: JsonEncoder[MeasureUnit] = DeriveJsonEncoder.gen[MeasureUnit]
}

object NutrientConversionFactor {
  implicit val decoder: JsonDecoder[NutrientConversionFactor] = DeriveJsonDecoder.gen[NutrientConversionFactor]
  implicit val encoder: JsonEncoder[NutrientConversionFactor] = DeriveJsonEncoder.gen[NutrientConversionFactor]
} 