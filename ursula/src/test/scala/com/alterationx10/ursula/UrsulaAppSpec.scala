package com.alterationx10.ursula

import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertCompletes}
import zio.{Scope, ZIO}

object UrsulaAppSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UrsulaAppSpec")(
      test("")(
        for {
          _ <- ZIO.logWarning("TestNotImplemented")
        } yield assertCompletes
      )
    )

}
