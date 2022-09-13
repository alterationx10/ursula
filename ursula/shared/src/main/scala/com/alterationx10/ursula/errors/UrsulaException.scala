package com.alterationx10.ursula.errors

import zio.*

trait UrsulaException extends Exception {
  val msg: String
  override def getMessage(): String = msg
  def printMessageZIO: Task[Unit]   = Console.printLine(getMessage)
}
