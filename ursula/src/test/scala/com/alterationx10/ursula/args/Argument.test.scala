package com.alterationx10.ursula.args

import zio.*
import zio.test.*

object ArgumentSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ArgumentSpec")(
      test("TODO") {
        assertTrue(true)
      }
    )
}
