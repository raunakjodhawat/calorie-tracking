package com.fooddataLoader.repository

import com.fooddataLoader.domain._
import com.fooddataLoader.ConnectionService
import zio._
import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.LocalDateTime

case class FoodEntity(
  fdcId: Long,
  description: String,
  createdAt: Timestamp = Timestamp.valueOf(LocalDateTime.now())
)

case class FoodPortionEntity(
  id: Long,
  foodFdcId: Long,
  value: Double,
  measureUnitId: Long,
  measureUnitName: String,
  measureUnitAbbreviation: String,
  modifier: Option[String],
  gramWeight: Double,
  sequenceNumber: Int,
  amount: Double,
  minYearAcquired: Option[Int]
)

case class NutrientConversionFactorEntity(
  id: Long,
  foodFdcId: Long,
  `type`: String,
  proteinValue: Option[Double],
  fatValue: Option[Double],
  carbohydrateValue: Option[Double],
  value: Option[Double]
)

trait FoodRepository {
  def insertFood(food: Food): ZIO[Scope, Throwable, Unit]
  def insertFoodPortions(fdcId: Long, portions: List[FoodPortion]): ZIO[Scope, Throwable, Unit]
  def insertNutrientConversionFactors(fdcId: Long, factors: List[NutrientConversionFactor]): ZIO[Scope, Throwable, Unit]
  def getAllFoods: ZIO[Scope, Throwable, List[FoodEntity]]
  def getFoodByFdcId(fdcId: Long): ZIO[Scope, Throwable, Option[FoodEntity]]
  def getFoodPortions(fdcId: Long): ZIO[Scope, Throwable, List[FoodPortionEntity]]
  def getNutrientConversionFactors(fdcId: Long): ZIO[Scope, Throwable, List[NutrientConversionFactorEntity]]
}

class FoodRepositoryLive(connectionService: ConnectionService) extends FoodRepository {

  override def insertFood(food: Food): ZIO[Scope, Throwable, Unit] = {
    val sql = """
      INSERT INTO foods (fdc_id, description, created_at)
      VALUES (?, ?, ?)
    """
    
    connectionService.getConnection.flatMap { conn =>
      ZIO.attemptBlocking {
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setLong(1, food.fdcId)
          stmt.setString(2, food.description)
          stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()))
          stmt.executeUpdate()
        } finally stmt.close()
      }.unit
    }
  }

  override def insertFoodPortions(fdcId: Long, portions: List[FoodPortion]): ZIO[Scope, Throwable, Unit] = {
    if (portions.isEmpty) ZIO.unit
    else {
      val sql = """
        INSERT INTO food_portions (id, food_fdc_id, "value", measure_unit_id, measure_unit_name,
                                 measure_unit_abbreviation, modifier, gram_weight, sequence_number,
                                 amount, min_year_acquired)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """
      
      connectionService.getConnection.flatMap { conn =>
        ZIO.attemptBlocking {
          val stmt = conn.prepareStatement(sql)
          try {
            portions.foreach { portion =>
              stmt.setLong(1, portion.id)
              stmt.setLong(2, fdcId)
              stmt.setDouble(3, portion.value)
              stmt.setLong(4, portion.measureUnit.id)
              stmt.setString(5, portion.measureUnit.name)
              stmt.setString(6, portion.measureUnit.abbreviation)
              portion.modifier match {
                case Some(mod) => stmt.setString(7, mod)
                case None      => stmt.setNull(7, java.sql.Types.VARCHAR)
              }
              stmt.setDouble(8, portion.gramWeight)
              stmt.setInt(9, portion.sequenceNumber)
              stmt.setDouble(10, portion.amount)
              portion.minYearAcquired match {
                case Some(year) => stmt.setInt(11, year)
                case None       => stmt.setNull(11, java.sql.Types.INTEGER)
              }
              stmt.addBatch()
            }
            stmt.executeBatch()
          } finally stmt.close()
        }.unit
      }
    }
  }

  override def insertNutrientConversionFactors(fdcId: Long, factors: List[NutrientConversionFactor]): ZIO[Scope, Throwable, Unit] = {
    if (factors.isEmpty) ZIO.unit
    else {
      val sql = """
        INSERT INTO nutrient_conversion_factors (food_fdc_id, type, protein_value, fat_value, 
                                               carbohydrate_value, "value")
        VALUES (?, ?, ?, ?, ?, ?)
      """
      
      connectionService.getConnection.flatMap { conn =>
        ZIO.attemptBlocking {
          val stmt = conn.prepareStatement(sql)
          try {
            factors.foreach { factor =>
              stmt.setLong(1, fdcId)
              stmt.setString(2, factor.`type`)
              factor.proteinValue match {
                case Some(pv) => stmt.setDouble(3, pv)
                case None     => stmt.setNull(3, java.sql.Types.DOUBLE)
              }
              factor.fatValue match {
                case Some(fv) => stmt.setDouble(4, fv)
                case None     => stmt.setNull(4, java.sql.Types.DOUBLE)
              }
              factor.carbohydrateValue match {
                case Some(cv) => stmt.setDouble(5, cv)
                case None     => stmt.setNull(5, java.sql.Types.DOUBLE)
              }
              factor.value match {
                case Some(v) => stmt.setDouble(6, v)
                case None    => stmt.setNull(6, java.sql.Types.DOUBLE)
              }
              stmt.addBatch()
            }
            stmt.executeBatch()
          } finally stmt.close()
        }.unit
      }
    }
  }

  override def getAllFoods: ZIO[Scope, Throwable, List[FoodEntity]] = {
    val sql = "SELECT * FROM foods"
    
    connectionService.getConnection.flatMap { conn =>
      ZIO.attemptBlocking {
        val stmt = conn.createStatement()
        try {
          val rs = stmt.executeQuery(sql)
          val foods = scala.collection.mutable.ListBuffer[FoodEntity]()
          while (rs.next()) {
            foods += FoodEntity(
              fdcId = rs.getLong("fdc_id"),
              description = rs.getString("description"),
              createdAt = rs.getTimestamp("created_at")
            )
          }
          foods.toList
        } finally stmt.close()
      }
    }
  }

  override def getFoodByFdcId(fdcId: Long): ZIO[Scope, Throwable, Option[FoodEntity]] = {
    val sql = "SELECT * FROM foods WHERE fdc_id = ?"
    
    connectionService.getConnection.flatMap { conn =>
      ZIO.attemptBlocking {
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setLong(1, fdcId)
          val rs = stmt.executeQuery()
          if (rs.next()) {
            Some(FoodEntity(
              fdcId = rs.getLong("fdc_id"),
              description = rs.getString("description"),
              createdAt = rs.getTimestamp("created_at")
            ))
          } else None
        } finally stmt.close()
      }
    }
  }

  override def getFoodPortions(fdcId: Long): ZIO[Scope, Throwable, List[FoodPortionEntity]] = {
    val sql = "SELECT * FROM food_portions WHERE food_fdc_id = ?"
    
    connectionService.getConnection.flatMap { conn =>
      ZIO.attemptBlocking {
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setLong(1, fdcId)
          val rs = stmt.executeQuery()
          val portions = scala.collection.mutable.ListBuffer[FoodPortionEntity]()
          while (rs.next()) {
            portions += FoodPortionEntity(
              id = rs.getLong("id"),
              foodFdcId = rs.getLong("food_fdc_id"),
              value = rs.getDouble("value"),
              measureUnitId = rs.getLong("measure_unit_id"),
              measureUnitName = rs.getString("measure_unit_name"),
              measureUnitAbbreviation = rs.getString("measure_unit_abbreviation"),
              modifier = {
                val value = rs.getString("modifier")
                if (rs.wasNull()) None else Some(value)
              },
              gramWeight = rs.getDouble("gram_weight"),
              sequenceNumber = rs.getInt("sequence_number"),
              amount = rs.getDouble("amount"),
              minYearAcquired = {
                val value = rs.getInt("min_year_acquired")
                if (rs.wasNull()) None else Some(value)
              }
            )
          }
          portions.toList
        } finally stmt.close()
      }
    }
  }

  override def getNutrientConversionFactors(fdcId: Long): ZIO[Scope, Throwable, List[NutrientConversionFactorEntity]] = {
    val sql = "SELECT * FROM nutrient_conversion_factors WHERE food_fdc_id = ?"
    
    connectionService.getConnection.flatMap { conn =>
      ZIO.attemptBlocking {
        val stmt = conn.prepareStatement(sql)
        try {
          stmt.setLong(1, fdcId)
          val rs = stmt.executeQuery()
          val factors = scala.collection.mutable.ListBuffer[NutrientConversionFactorEntity]()
          while (rs.next()) {
            factors += NutrientConversionFactorEntity(
              id = rs.getLong("id"),
              foodFdcId = rs.getLong("food_fdc_id"),
              `type` = rs.getString("type"),
              proteinValue = {
                val value = rs.getDouble("protein_value")
                if (rs.wasNull()) None else Some(value)
              },
              fatValue = {
                val value = rs.getDouble("fat_value")
                if (rs.wasNull()) None else Some(value)
              },
              carbohydrateValue = {
                val value = rs.getDouble("carbohydrate_value")
                if (rs.wasNull()) None else Some(value)
              },
              value = {
                val value = rs.getDouble("value")
                if (rs.wasNull()) None else Some(value)
              }
            )
          }
          factors.toList
        } finally stmt.close()
      }
    }
  }
}

object FoodRepository {
  def insertFood(food: Food): ZIO[FoodRepository & Scope, Throwable, Unit] =
    ZIO.serviceWithZIO[FoodRepository](_.insertFood(food))

  def insertFoodPortions(fdcId: Long, portions: List[FoodPortion]): ZIO[FoodRepository & Scope, Throwable, Unit] =
    ZIO.serviceWithZIO[FoodRepository](_.insertFoodPortions(fdcId, portions))

  def insertNutrientConversionFactors(fdcId: Long, factors: List[NutrientConversionFactor]): ZIO[FoodRepository & Scope, Throwable, Unit] =
    ZIO.serviceWithZIO[FoodRepository](_.insertNutrientConversionFactors(fdcId, factors))

  def getAllFoods: ZIO[FoodRepository & Scope, Throwable, List[FoodEntity]] =
    ZIO.serviceWithZIO[FoodRepository](_.getAllFoods)

  def getFoodByFdcId(fdcId: Long): ZIO[FoodRepository & Scope, Throwable, Option[FoodEntity]] =
    ZIO.serviceWithZIO[FoodRepository](_.getFoodByFdcId(fdcId))

  def getFoodPortions(fdcId: Long): ZIO[FoodRepository & Scope, Throwable, List[FoodPortionEntity]] =
    ZIO.serviceWithZIO[FoodRepository](_.getFoodPortions(fdcId))

  def getNutrientConversionFactors(fdcId: Long): ZIO[FoodRepository & Scope, Throwable, List[NutrientConversionFactorEntity]] =
    ZIO.serviceWithZIO[FoodRepository](_.getNutrientConversionFactors(fdcId))

  val live: ZLayer[ConnectionService, Nothing, FoodRepository] =
    ZLayer {
      for {
        connectionService <- ZIO.service[ConnectionService]
      } yield new FoodRepositoryLive(connectionService)
    }
} 