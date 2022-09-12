package com.alterationx10.ursula

import com.alterationx10.ursula.command.Command
import os.{Path, Source}
import utest.*

object UrsulaAppSpec extends TestSuite {

  object TestProgram extends UrsulaApp {
    lazy val tmpDir: Path = os.temp.dir(prefix = "ursula-test-UrsulaAppSpec")
    lazy val tmpConfig: Path = os.temp(contents = Source.WritableSource("{}"), dir = tmpDir)
    override lazy val configDirectory: String = tmpDir.toString()
    override lazy val configFile: String = tmpConfig.last
    override val commands: Seq[Command] = Seq.empty
  }

  override def tests: Tests = Tests {
    test("Test program compiles and runs") {
      // Calling .main might not be the best?
      TestProgram.main(Array.empty[String])
    }
  }
}
