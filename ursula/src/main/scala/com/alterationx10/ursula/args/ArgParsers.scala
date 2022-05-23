package com.alterationx10.ursula.args

import zio._

trait ArgParsers {

  def hasFlag(args: Chunk[String])(flag: Flag): Boolean =
    args.contains(flag._sk) || args.contains(flag._lk)

  def getFlagArgument[T](
      args: Chunk[String]
  )(flag: Flag)(op: String => T): Option[T] = {
    assert(flag.isArgument)
    args
      .dropUntil(a => a == flag._sk || a == flag._lk)
      .headOption
      .map(op)
  }

  def getArgument[T](
      args: Chunk[String]
  )(argument: Argument)(op: String => T): Option[T] =
    args
      .dropUntil(_ == argument.key)
      .headOption
      .map(op)

}

object ArgParsers extends ArgParsers
