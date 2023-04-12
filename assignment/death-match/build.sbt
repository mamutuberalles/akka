val scala3Version = "3.2.0"
val AkkaVersion = "2.7.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "death-match",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,

    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

    //braryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
  )
