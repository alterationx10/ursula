package com.alterationx10.ursula.services

import zio.*
import zio.Console.ConsoleLive
import zio.internal.Platform

import java.io.IOException

object TTY {

  def getPlatformConsole: Console =
    if (Platform.isNative) TTYLive else ConsoleLive

  object TTYLive extends Console {

    override def print(
        line: => Any
    )(implicit trace: Trace): IO[IOException, Unit] = ZIO
      .attempt(scala.Console.print(line))
      .mapError(e => new IOException(e.getMessage, e))

    override def printError(line: => Any)(implicit
        trace: Trace
    ): IO[IOException, Unit] = print(line)

    override def printLine(line: => Any)(implicit
        trace: Trace
    ): IO[IOException, Unit] = ZIO
      .attempt(scala.Console.println(line))
      .mapError(e => new IOException(e.getMessage, e))

    override def printLineError(line: => Any)(implicit
        trace: Trace
    ): IO[IOException, Unit] = printLine(line)

    override def readLine(implicit trace: Trace): IO[IOException, String] = ZIO
      .attempt(scala.io.StdIn.readLine())
      .mapError(e => new IOException(e.getMessage, e))

  }

  def print(line: => Any)(implicit trace: Trace): IO[IOException, Unit] =
    TTYLive.print(line)

  def printError(line: => Any)(implicit trace: Trace): IO[IOException, Unit] =
    TTYLive.printError(line)

  def printLine(line: => Any)(implicit trace: Trace): IO[IOException, Unit] =
    TTYLive.printLine(line)

  def printLineError(line: => Any)(implicit
      trace: Trace
  ): IO[IOException, Unit] = TTYLive.printLineError(line)

  def readLine(implicit trace: Trace): IO[IOException, String] =
    TTYLive.readLine

}
