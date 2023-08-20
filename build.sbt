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

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) =>
    xs map {
      _.toLowerCase
    } match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) => MergeStrategy.discard
      case _ => MergeStrategy.last
    }
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}