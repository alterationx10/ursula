package com.alterationx10.ursula

import zio.*
import zio.test.*

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
