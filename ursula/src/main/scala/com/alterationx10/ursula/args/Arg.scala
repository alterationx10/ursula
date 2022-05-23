package com.alterationx10.ursula.args

import zio._
import java.io.IOException

sealed trait Arg {
  val description: String
}

case class Flag(
    shortKey: String,
    longKey: String,
    description: String,
    isArgument: Boolean = false
) extends Arg {
  final val _sk: String = s"-$shortKey"
  final val _lk: String = s"--$longKey"

  private val argHolder: String = if (isArgument) " [arg]" else ""
  val describe: String = s"\t${_sk}, ${_lk}$argHolder\t$description"
  val describeZIO = Console.printLine(describe)
}

case class Argument(
    key: String,
    description: String
) extends Arg {
    val describe: String = s"\t$key [arg]\t$description"
    val describeZIO: IO[IOException,Unit] = Console.printLine(describe)
}
