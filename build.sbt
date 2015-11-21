name := """restprimes"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "junit" % "junit" % "4.12" % "test",
  "pl.matisoft" %% "swagger-play24" % "1.4",
  "xstream" % "xstream" % "1.2.2",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.13" % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

jacoco.settings

jacoco.excludes in jacoco.Config := Seq("views*", "*Routes*", "controllers*routes*", "*Reverse*",
  "*javascript*", "*ref*", "*routes")

parallelExecution in jacoco.Config := false

javaOptions in Test += "-Dlogger.file=conf/logback.xml"