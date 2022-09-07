object Versioning {

  val tagWithQualifier: String => String => String =
    qualifier =>
      tagVersion => s"%s.%s.%s-${qualifier}%s".format(tagVersion.split("\\.")*)

  val tagAlpha: String => String     = tagWithQualifier("a")
  val tagBeta: String => String      = tagWithQualifier("b")
  val tagMilestone: String => String = tagWithQualifier("m")
  val tagRC: String => String        = tagWithQualifier("rc")
  val tagSnapshot: String => String  = tagVersion =>
    s"%s.%s.%s-SNAPSHOT".format(tagVersion.split("\\.")*)

  val defaultVersion: String = "0.0.0-SNAPSHOT"
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

}
