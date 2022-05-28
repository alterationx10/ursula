package com.alterationx10.ursula.command

import com.alterationx10.ursula.args.{Argument, Flag}
import com.alterationx10.ursula.args.builtin.Flags
import zio._

import scala.annotation.tailrec

trait Command[A] {
  val description: String
  val usage: String
  val examples: Seq[String]
  val trigger: String
  val flags: Seq[Flag[_]]
  val arguments: Seq[Argument[_]]
  val hidden: Boolean           = false
  val isDefaultCommand: Boolean = false
  def action(args: Chunk[String]): Task[A]

  private def hasBooleanFlag(a: String) =
    flags
      .filter(!_.expectsArgument)
      .exists(f => f._sk == a || f._lk == a)

  private def hasArgumentFlag(a: String) =
    flags
      .filter(_.expectsArgument)
      .exists(f => f._sk == a || f._lk == a)

  /** Strips flags and their arguments from the cli arguments, which can then be
    * parsed for Arguments
    * @param args
    *   The cli arguments
    */
  def stripFlags(args: Chunk[String]): Chunk[String] = {
    @tailrec
    def loop(a: Chunk[String], r: Chunk[String]): Chunk[String] = {
      a.headOption match {
        case Some(h) => {
          if (hasBooleanFlag(h)) {
            loop(a.drop(1), r)
          } else if (hasArgumentFlag(a)) {
            // TODO what if this has a default value?
            loop(a.drop(2), r)
          } else {
            loop(a.drop(1), r.appended(h))
          }
        }
        case None    => r
      }
    }
    loop(args, Chunk.empty)
  }

  /**
    * Prints documentation
    * @return
    */
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
