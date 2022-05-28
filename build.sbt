// https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables
// https://www.scala-sbt.org/1.x/docs/Publishing.html#Credentials

val tagWithQualifier: String => String => String =
  qualifier =>
    tagVersion => s"%s.%s.%s-${qualifier}%s".format(tagVersion.split("\\."): _*)

val tagAlpha: String => String     = tagWithQualifier("a")
val tagBeta: String => String      = tagWithQualifier("b")
val tagMilestone: String => String = tagWithQualifier("m")
val tagRC: String => String        = tagWithQualifier("rc")

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
      case 'v' => t.tail               // Production build, should be v1.2.3
      case _   => defaultVersion
    }
  }
  .getOrElse(defaultVersion)

ThisBuild / organization := "com.alterationx10"
ThisBuild / version           := versionFromTag
ThisBuild / scalaVersion      := "2.13.8"
ThisBuild / publish / skip    := true
ThisBuild / publishMavenStyle := true
ThisBuild / versionScheme     := Some("early-semver")
ThisBuild / publishTo         := Some(
  "GitHub Package Registry " at "https://maven.pkg.github.com/alterationx10/ursula"
)
ThisBuild / credentials += Credentials(
  "GitHub Package Registry",                  // realm
  "maven.pkg.github.com",                     // host
  "alterationx10",                            // user
  sys.env.getOrElse("GITHUB_TOKEN", "abc123") // password
)

val zioVersion: String = "2.0.0-RC6"

lazy val ursula = project
  .in(file("ursula"))
  .settings(
    name           := "ursula",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % "test",
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
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
