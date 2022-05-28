package com.alterationx10.ursula.args

import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertCompletes}
import zio.{Scope, ZIO}

object ArgumentSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ArgumentSpec")(
      test("")(
        for {
          _ <- ZIO.fail("TestNotImplemented")
        } yield assertCompletes
      )
    )

}
