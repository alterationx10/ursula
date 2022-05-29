package com.alterationx10.ursula.args

import zio._
import zio.test._

object FlagSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FlagSpec")(
      test("")(
        for {
          _ <- ZIO.logWarning("TestNotImplemented")
        } yield assertCompletes
      )
    )

}
