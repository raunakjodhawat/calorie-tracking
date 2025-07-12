package com.foodbackend.repository

import zio._
import java.sql.Connection
import com.foodbackend.ConnectionService

class DatabaseSetup(connectionService: ConnectionService) {
  def createTables: ZIO[ConnectionService & Scope, Throwable, Unit] = {
    val createFoodsTable = """
      CREATE TABLE IF NOT EXISTS foods (
        fdc_id BIGINT PRIMARY KEY,
        description TEXT NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
      )
    """

    val createFoodPortionsTable = """
      CREATE TABLE IF NOT EXISTS food_portions (
        id BIGINT PRIMARY KEY,
        food_fdc_id BIGINT NOT NULL,
        value DOUBLE PRECISION NOT NULL,
        measure_unit_id BIGINT NOT NULL,
        measure_unit_name VARCHAR(255) NOT NULL,
        measure_unit_abbreviation VARCHAR(255) NOT NULL,
        modifier VARCHAR(255) NOT NULL,
        gram_weight DOUBLE PRECISION NOT NULL,
        sequence_number INTEGER NOT NULL,
        amount DOUBLE PRECISION NOT NULL,
        min_year_acquired INTEGER,
        FOREIGN KEY (food_fdc_id) REFERENCES foods(fdc_id) ON DELETE CASCADE
      )
    """

    val createNutrientConversionFactorsTable = """
      CREATE TABLE IF NOT EXISTS nutrient_conversion_factors (
        id BIGSERIAL PRIMARY KEY,
        food_fdc_id BIGINT NOT NULL,
        type VARCHAR(255) NOT NULL,
        protein_value DOUBLE PRECISION,
        fat_value DOUBLE PRECISION,
        carbohydrate_value DOUBLE PRECISION,
        value DOUBLE PRECISION,
        FOREIGN KEY (food_fdc_id) REFERENCES foods(fdc_id) ON DELETE CASCADE
      )
    """

    def exec(sql: String): ZIO[ConnectionService & Scope, Throwable, Unit] =
      connectionService.getConnection.flatMap { conn =>
        ZIO.attemptBlocking {
          val stmt = conn.createStatement()
          try stmt.execute(sql)
          finally stmt.close()
        }.unit
      }

    for {
      _ <- exec(createFoodsTable)
      _ <- exec(createFoodPortionsTable)
      _ <- exec(createNutrientConversionFactorsTable)
    } yield ()
  }

  def dropTables: ZIO[ConnectionService & Scope, Throwable, Unit] = {
    val dropNutrientConversionFactorsTable = "DROP TABLE IF EXISTS nutrient_conversion_factors CASCADE"
    val dropFoodPortionsTable = "DROP TABLE IF EXISTS food_portions CASCADE"
    val dropFoodsTable = "DROP TABLE IF EXISTS foods CASCADE"

    def exec(sql: String): ZIO[ConnectionService & Scope, Throwable, Unit] =
      connectionService.getConnection.flatMap { conn =>
        ZIO.attemptBlocking {
          val stmt = conn.createStatement()
          try stmt.execute(sql)
          finally stmt.close()
        }.unit
      }

    for {
      _ <- exec(dropNutrientConversionFactorsTable)
      _ <- exec(dropFoodPortionsTable)
      _ <- exec(dropFoodsTable)
    } yield ()
  }
}

object DatabaseSetup {
  def createTables: ZIO[ConnectionService & Scope, Throwable, Unit] =
    ZIO.serviceWithZIO[ConnectionService] { connectionService =>
      new DatabaseSetup(connectionService).createTables
    }

  def dropTables: ZIO[ConnectionService & Scope, Throwable, Unit] =
    ZIO.serviceWithZIO[ConnectionService] { connectionService =>
      new DatabaseSetup(connectionService).dropTables
    }
} 