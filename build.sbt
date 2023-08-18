ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "review-ranker",
    libraryDependencies ++=
      Dependencies.AKKA_DEPENDENCIES
        ++ Dependencies.CIRCE_DEPENDENCIES
        :+ Dependencies.JSOUP_DEPENDENCY
        :+ Dependencies.LOGBACK_DEPENDENCY
  )
