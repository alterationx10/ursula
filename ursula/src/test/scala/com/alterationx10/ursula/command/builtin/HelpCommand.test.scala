package com.alterationx10.ursula.command.builtin

import zio.*
import zio.test.*
import os.move.over

object HelpCommandSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("HelpCommandSpec")(
      test("TODO") {
        assertTrue(true)
      }
    )

}
