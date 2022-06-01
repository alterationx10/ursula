package com.alterationx10.ursula.errors

import zio.Console
import zio.IO
import java.io.IOException

trait UrsulaException extends Exception {
  val msg: String
  override def getMessage(): String          = msg
  def printMessageZIO: IO[IOException, Unit] = Console.printLine(getMessage)
}
