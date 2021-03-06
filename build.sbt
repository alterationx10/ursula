// https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables
// https://www.scala-sbt.org/1.x/docs/Publishing.html#Credentials

val tagWithQualifier: String => String => String =
  qualifier =>
    tagVersion => s"%s.%s.%s-${qualifier}%s".format(tagVersion.split("\\.")*)

val tagAlpha: String => String     = tagWithQualifier("a")
val tagBeta: String => String      = tagWithQualifier("b")
val tagMilestone: String => String = tagWithQualifier("m")
val tagRC: String => String        = tagWithQualifier("rc")
val tagSnapshot: String => String  = tagVersion =>
  s"%s.%s.%s-SNAPSHOT".format(tagVersion.split("\\.")*)

val defaultVersion: String = "0.0.0-a0"
val versionFromTag: String = sys.env
  .get("GITHUB_REF_TYPE")
  .filter(_ == "tag")
  .flatMap(_ => sys.env.get("GITHUB_REF_NAME"))
  .flatMap { t =>
    t.headOption.map {
      case 'a' => tagAlpha(t.tail)     // Alpha build, a1.2.3.4
      case 'b' => tagBeta(t.tail)      // Beta build, b1.2.3.4
      case 'm' => tagMilestone(t.tail) // Milestone build, m1.2.3.4
      case 'r' => tagRC(t.tail)        // RC build, r1.2.3.4
      case 's' => tagSnapshot(t.tail)  // SNAPSHOT build, s1.2.3
      case 'v' => t.tail               // Production build, should be v1.2.3
      case _   => defaultVersion
    }
  }
  .getOrElse(defaultVersion)

ThisBuild / organization := "com.alterationx10"
ThisBuild / version                       := versionFromTag
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

ThisBuild / Test / fork                   := true
ThisBuild / Test / envVars += "TEST_FLAG" -> "abc"

val zioVersion: String     = "2.0.0"
val zioJsonVersion: String = "0.3.0-RC8"

lazy val ursula = project
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
    fork           := true,
    publish / skip := false
  )

lazy val example = project
  .in(file("example"))
  .settings(
    publishArtifact := false,
    fork            := true
  )
  .dependsOn(ursula)
