ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "food-data-loader",
    libraryDependencies ++= Seq(
      // ZIO
      "dev.zio" %% "zio" % "2.0.18",
      "dev.zio" %% "zio-streams" % "2.0.18",
      "dev.zio" %% "zio-json" % "0.6.0",
      
      // Database
      "org.postgresql" % "postgresql" % "42.7.1",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.0",
      "com.h2database" % "h2" % "2.2.224" % Test,
      
      // Configuration
      "dev.zio" %% "zio-config" % "4.0.0",
      "dev.zio" %% "zio-config-typesafe" % "4.0.0",
      "dev.zio" %% "zio-config-magnolia" % "4.0.0",
      
      // Logging
      "dev.zio" %% "zio-logging" % "2.1.14",
      "dev.zio" %% "zio-logging-slf4j" % "2.1.14",
      
      // Testing
      "dev.zio" %% "zio-test" % "2.0.18" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.18" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    
    // Scoverage settings
    coverageEnabled := true,
    coverageMinimumStmtTotal := 100,
    coverageMinimumBranchTotal := 100,
    coverageMinimumStmtPerPackage := 100,
    coverageMinimumBranchPerPackage := 100,
    coverageMinimumStmtPerFile := 100,
    coverageMinimumBranchPerFile := 100,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  ) 