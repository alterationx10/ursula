package com.alterationx10.ursula.services

import zio.*

object UrsulaServices {

  type UrsulaServices = Config

  val live: ZLayer[Any, Throwable, UrsulaServices] = ZLayer.make[Config](
    ConfigLive.live
  )

}
