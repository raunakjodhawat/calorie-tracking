package com.fooddataLoader

import com.fooddataLoader.config.AppConfig
import com.fooddataLoader.loader.FoodDataLoader
import com.fooddataLoader.repository.{DatabaseSetup, FoodRepository}
import zio._
import zio.logging._
import java.sql.Connection
import javax.sql.DataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

trait ConnectionService {
  def getConnection: ZIO[Scope, Throwable, Connection]
}

class ConnectionServiceLive(dataSource: DataSource) extends ConnectionService {
  override def getConnection: ZIO[Scope, Throwable, Connection] = 
    ZIO.acquireRelease(
      ZIO.attemptBlocking(dataSource.getConnection)
    )(conn => ZIO.attemptBlocking(conn.close()).orDie)
}

object Main extends ZIOAppDefault {

  def run: ZIO[Any, Throwable, Unit] = {
    val program = for {
      _ <- ZIO.logInfo("Starting Food Backend Application")
      _ <- DatabaseSetup.dropTables
      _ <- ZIO.logInfo("Database tables dropped successfully")
      _ <- DatabaseSetup.createTables
      _ <- ZIO.logInfo("Database tables created successfully")
      _ <- FoodDataLoader.loadFoodData
      _ <- ZIO.logInfo("Food data loading completed")
    } yield ()

    program.provide(
      // Configuration
      AppConfig.live,
      
      // Database connection pool
      createDataSourceLayer,
      
      // Connection service
      connectionServiceLayer,
      
      // Repository
      FoodRepository.live,
      
      // Loader
      FoodDataLoader.live,
      
      // Scope for resource management
      Scope.default,
      
      // Logging
      Runtime.removeDefaultLoggers >>> consoleLogger()
    )
  }

  private val createDataSourceLayer: ZLayer[AppConfig, Throwable, DataSource] =
    ZLayer {
      for {
        config <- ZIO.service[AppConfig]
        dataSource <- ZIO.attempt {
          val hikariConfig = new HikariConfig()
          hikariConfig.setJdbcUrl(s"jdbc:postgresql://${config.database.host}:${config.database.port}/${config.database.database}")
          hikariConfig.setUsername(config.database.username)
          hikariConfig.setPassword(config.database.password)
          hikariConfig.setMaximumPoolSize(50)
          hikariConfig.setMinimumIdle(5)
          hikariConfig.setConnectionTimeout(60000)
          hikariConfig.setIdleTimeout(600000)
          hikariConfig.setMaxLifetime(1800000)
          new HikariDataSource(hikariConfig)
        }
      } yield dataSource
    }

  private val connectionServiceLayer: ZLayer[DataSource, Nothing, ConnectionService] =
    ZLayer {
      for {
        dataSource <- ZIO.service[DataSource]
      } yield new ConnectionServiceLive(dataSource)
    }
} 