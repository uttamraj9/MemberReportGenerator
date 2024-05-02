ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
    name := "MemberReportGenerator",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.1.3",
      "org.apache.spark" %% "spark-sql" % "3.1.3",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test
    ),
    resolvers += "Apache Releases" at "https://repository.apache.org/content/repositories/releases/"
  )