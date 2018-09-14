import Dependencies._


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.6",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "cats-effect-doobie-cancel",
    libraryDependencies ++= Seq(scalaTest % Test,
      "mysql" % "mysql-connector-java" % "8.0.12",
      "com.wix" % "wix-embedded-mysql" % "4.1.2",
      "org.tpolecat" %% "doobie-core" % "0.6.0-M2")
  )


