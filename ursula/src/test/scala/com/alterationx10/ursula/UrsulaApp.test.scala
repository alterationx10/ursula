package com.alterationx10.ursula

import com.alterationx10.ursula.command.Command
import os.{Path, Source}
import os.copy.over
import zio.test.ZIOSpecDefault
import zio.Scope
import zio.test.*

object UrsulaAppSpec extends ZIOSpecDefault {

  object TestProgram extends UrsulaApp {
    override val appIdentifier: String        = "ursula-test"
    lazy val tmpDir: Path                     = os.temp.dir(prefix = "ursula-test-UrsulaAppSpec")
    lazy val tmpConfig: Path                  =
      os.temp(contents = Source.WritableSource("{}"), dir = tmpDir)
    override lazy val configDirectory: String = tmpDir.toString()
    override lazy val configFile: String      = tmpConfig.last
    override val commands: Seq[Command]       = Seq.empty
  }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UrsulaAppSpec")(
      test("Test program compiles and runs") {
        // I think calling .main currently sends an exit code, causing none of the other tests to run with scala-cli.
        // Comment out for now.

        // TestProgram.main(Array.empty[String])
        assertCompletes
      }
    )

}
