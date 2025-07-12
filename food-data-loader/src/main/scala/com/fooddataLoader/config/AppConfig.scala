package com.fooddataLoader.config

import zio._
import zio.config._
import zio.config.magnolia._
import zio.config.typesafe._

case class DatabaseConfig(
  host: String,
  port: Int,
  database: String,
  username: String,
  password: String
)

case class AppConfig(
  database: DatabaseConfig,
  foodDataPath: String
)

object AppConfig {
  private val config = TypesafeConfigProvider
    .fromResourcePath()
    .load(deriveConfig[AppConfig].nested("app"))

  val live: ZLayer[Any, Config.Error, AppConfig] = ZLayer(config)
} 