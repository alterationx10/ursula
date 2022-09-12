package com.alterationx10.ursula.errors

import com.alterationx10.ursula.services.TTY
import zio.Task

trait UrsulaException extends Exception {
  val msg: String
  override def getMessage(): String = msg
  def printMessageZIO: Task[Unit]   = TTY.printLine(getMessage)
}
