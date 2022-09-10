package com.alterationx10.ursula.services

import zio.*

trait TTY {
  def printLine(str: String): Task[Unit]
}

object TTY extends TTY {
  override def printLine(str: String): Task[Unit] = ZIO.attempt(println(str))
}
