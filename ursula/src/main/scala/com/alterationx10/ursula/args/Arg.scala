package com.alterationx10.ursula.args

import zio._
import java.io.IOException

sealed trait Arg {
  val description: String
  val isRequired: Boolean

  lazy val reqHolder: String = if (isRequired) " ! required" else ""

}

case class Flag(
    shortKey: String,
    longKey: String,
    description: String,
    isArgument: Boolean = false,
    isRequired: Boolean = false,
    conflicts: Seq[Flag] = Seq.empty
) extends Arg {
  final val _sk: String = s"-$shortKey"
  final val _lk: String = s"--$longKey"

  val conflictsWith: String =
    s"\t^-- conflicts with:\n${conflicts.map(f => s"\t\t${f._sk}, ${f._lk}").mkString("\n")}"
  val conflictsWithZIO: IO[IOException, Unit] = Console.printLine(conflictsWith)

  private val argHolder: String = if (isArgument) " [arg]" else ""
  val describe: String = s"\t${_sk}, ${_lk}$argHolder\t$description$reqHolder"
  val describeZIO: IO[IOException, Unit] =
    Console.printLine(describe) *> ZIO
      .when(conflicts.nonEmpty)(conflictsWithZIO)
      .unit

}

case class Argument(
    key: String,
    description: String,
    isRequired: Boolean = false,
    conflicts: Seq[Argument] = Seq.empty
) extends Arg {
  val describe: String = s"\t$key [arg]\t$description$reqHolder"
  val describeZIO: IO[IOException, Unit] = Console.printLine(describe)

  val conflictsWith: String =
    s"\t^-- conflicts with:\n${conflicts.map(a => s"\t\t$a").mkString("\n")}"

  val conflictsWithZIO: IO[IOException, Unit] = Console.printLine(conflictsWith)
}
