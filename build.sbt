ThisBuild / organization                  := "com.alterationx10"
ThisBuild / version                       := Versioning.versionFromTag
ThisBuild / scalaVersion                  := "3.1.3"
ThisBuild / crossScalaVersions ++= Seq("2.13.8", "3.1.3")
ThisBuild / publish / skip                := true
ThisBuild / publishMavenStyle             := true
ThisBuild / versionScheme                 := Some("early-semver")
ThisBuild / publishTo                     := Some(
  "Cloudsmith API" at "https://maven.cloudsmith.io/alterationx10/ursula/"
)
ThisBuild / pomIncludeRepository          := { x => false }
ThisBuild / credentials += Credentials(
  "Cloudsmith API",                                      // realm
  "maven.cloudsmith.io",                                 // host
  sys.env.getOrElse("CLOUDSMITH_USER", "alterationx10"), // user
  sys.env.getOrElse("CLOUDSMITH_TOKEN", "abc123")        // password
)
ThisBuild / scalacOptions ++= {
  Seq(
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:implicitConversions"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) =>
      Seq(
        "-unchecked"
      )
    case _            =>
      Seq(
        "-deprecation",
        "-Xfatal-warnings",
        "-Wunused:imports",
        "-Wvalue-discard",
        "-Xsource:3"
      )
  })
}

ThisBuild / Test / envVars += "TEST_FLAG" -> "abc"

val zioVersion: String     = "2.0.2"
val zioJsonVersion: String = "0.3.0-RC8"

lazy val ursula = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("ursula"))
  .settings(
    name           := "ursula",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % "test",
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test",
      "dev.zio" %% "zio-streams"  % zioVersion,
      "dev.zio" %% "zio-json"     % zioJsonVersion
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    publish / skip := false
  )
  .jvmSettings(
    fork := true
  )
  .jsSettings(
    fork := false
  )
  .nativeSettings(
    fork := false
  )

lazy val example = project
  .in(file("example"))
  .settings(
    publishArtifact := false,
    fork            := true
  )
  .dependsOn(ursula.jvm)

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")
