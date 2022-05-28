package com.alterationx10.ursula.args.builtin

import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertCompletes}
import zio.{Scope, ZIO}

object FlagsSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FlagsSpec")(
      test("")(
        for {
          _ <- ZIO.fail("TestNotImplemented")
        } yield assertCompletes
      )
    )

}
