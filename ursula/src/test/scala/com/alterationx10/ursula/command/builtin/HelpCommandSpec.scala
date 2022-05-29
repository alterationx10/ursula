package com.alterationx10.ursula.command.builtin

import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertCompletes}
import zio.{Scope, ZIO}

object HelpCommandSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("HelpCommandSpec")(
      test("")(
        for {
          _ <- ZIO.logWarning("TestNotImplemented")
        } yield assertCompletes
      )
    )

}
