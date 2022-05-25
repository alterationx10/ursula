ThisBuild / organization := "com.alterationx10"
ThisBuild / version      := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

lazy val ursula = project
  .in(file("ursula"))
  .settings(
    name := "ursula",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.0-RC6"
    ),
    fork := true
  )

lazy val example = project
  .in(file("example"))
  .settings(
    publishArtifact := false,
    fork            := true
  )
  .dependsOn(ursula)
