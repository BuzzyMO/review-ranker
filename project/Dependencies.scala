import sbt._

object Dependencies {
  val AKKA_VERSION = "2.8.0"
  val AKKA_HTTP_VERSION = "10.5.0"

  val AKKA_DEPENDENCIES = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % AKKA_VERSION,
    "com.typesafe.akka" %% "akka-http" % AKKA_HTTP_VERSION
  )
}
