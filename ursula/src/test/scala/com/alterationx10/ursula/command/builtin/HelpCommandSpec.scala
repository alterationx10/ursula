package com.alterationx10.ursula.command.builtin

import zio.*
import zio.test.*

object HelpCommandSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("HelpCommandSpec")(
      test("")(
        for {
          _ <- ZIO.logWarning("TestNotImplemented")
        } yield assertCompletes
      )
    )

}
