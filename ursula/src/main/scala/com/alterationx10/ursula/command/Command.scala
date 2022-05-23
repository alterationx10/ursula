package com.alterationx10.ursula.command

import zio._
import com.alterationx10.ursula.args._
import com.alterationx10.ursula.args.builtin.Flags

trait Command[A] {
  val description: String
  val usage: String
  val examples: Seq[String]
  val trigger: String
  val flags: Seq[Flag]
  val arguments: Seq[Argument]
  val hidden: Boolean = false
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
    _ <- Console.printLine(s"Usage: $usage")
    _ <- Console.printLine("Examples:")
    _ <- ZIO.foreach(examples)(e => Console.printLine(e))
  } yield ()

  final def processedAction(args: Chunk[String]): Task[Unit] = if (
    args.contains("--help") || args.contains("-h")
  ) {
    printHelp
  } else action(args).unit

}

trait UnitCommand extends Command[Unit]
