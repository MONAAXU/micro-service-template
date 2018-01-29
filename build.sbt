name := "micro-service-template"

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.11",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.11" % Test,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scala-lang" % "scala-reflect" % "2.12.4"
)