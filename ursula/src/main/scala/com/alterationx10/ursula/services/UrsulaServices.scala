package com.alterationx10.ursula.services

import zio.*
import com.alterationx10.ursula.services.config.UrsulaConfig
import com.alterationx10.ursula.services.config.UrsulaConfigLive

object UrsulaServices {

  type UrsulaServices = UrsulaConfig

  val live: ZLayer[Any, Throwable, UrsulaServices] = ZLayer.make[UrsulaConfig](
    UrsulaConfigLive.live
  )

}
