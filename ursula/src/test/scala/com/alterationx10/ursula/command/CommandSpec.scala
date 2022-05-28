package com.alterationx10.ursula.command

import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertCompletes}
import zio.{Scope, ZIO}

object CommandSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CommandSpec")(
      test("")(
        for {
          _ <- ZIO.fail("TestNotImplemented")
        } yield assertCompletes
      )
    )

}
