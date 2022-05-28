package com.alterationx10.ursula.command

import com.alterationx10.ursula.args.{Flag, Argument}
import com.alterationx10.ursula.args.builtin.Flags
import zio._

trait Command[A] {
  val description: String
  val usage: String
  val examples: Seq[String]
  val trigger: String
  val flags: Seq[Flag[_]]
  val arguments: Seq[Argument]
  val hidden: Boolean           = false
  val isDefaultCommand: Boolean = false
  def action(args: Chunk[String]): Task[A]

  final def printHelp: Task[Unit] = for {
    _ <- Console.printLine(s"$trigger:\t$description")
    _ <- ZIO.when(flags.nonEmpty) {
           Console.printLine(s"Flags:") *>
             ZIO.foreach(flags)(_.describeZIO)
         }
    _ <- ZIO.when(arguments.nonEmpty) {
           Console.printLine(s"Arguments:") *>
             ZIO.foreach(arguments)(_.describeZIO)
         }
    _ <- Console.printLine(s"Usage:\n\t$usage")
    _ <- Console.printLine("Examples:")
    _ <- ZIO.foreach(examples)(e => Console.printLine(s"\t$e"))
  } yield ()

  final def processedAction(args: Chunk[String]): Task[Unit] = if (
    Flags.helpFlag.isPresent(args)
  ) {
    printHelp
  } else action(args).unit

}

trait UnitCommand extends Command[Unit]
