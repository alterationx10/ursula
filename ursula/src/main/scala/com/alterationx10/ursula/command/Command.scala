package com.alterationx10.ursula.command

import com.alterationx10.ursula.args.{Argument, Flag}
import com.alterationx10.ursula.args.builtin.Flags

import scala.annotation.tailrec
import zio.*

trait Command[A] {
  val description: String
  val usage: String
  val examples: Seq[String]
  val trigger: String
  val flags: Seq[Flag[?]]
  val arguments: Seq[Argument[?]]
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
          if hasBooleanFlag(h) then {
            loop(a.drop(1), r)
          } else if hasArgumentFlag(h) then {
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

  /** Prints documentation
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

  private final def unrecognizedFlags(args: Chunk[String]): Boolean = {
    val flagTriggers: Seq[String] =
      flags.flatMap(f => Seq(f._sk, f._lk)).distinct
    args.filter(_.startsWith("-")).exists(a => !flagTriggers.contains(a))
  }

  private final def conflictingFlags(presentFlags: Seq[Flag[?]]): Boolean = {
    presentFlags
      .map { f =>
        presentFlags.flatMap(_.exclusive).flatten.contains(f)
      }
      .fold(false)(_ || _)
  }

  private final def missingRequiredFlags(
      presentFlags: Seq[Flag[?]]
  ): Boolean = {
    flags.filterNot(presentFlags.toSet).exists(_.required)
  }

  final def processedAction(args: Chunk[String]): Task[Unit] = {
    for {
      _            <- ZIO.cond(!Flags.helpFlag.isPresent(args), (), new Exception())
      _            <- ZIO
                        .cond(!unrecognizedFlags(args), (), new Exception())
                        .tapError(_ =>
                          Console.printLine("You have given an unrecognized flag.") *>
                            Console.printLine(s"> ${args.mkString(" ")}")
                        )
      presentFlags <- ZIO.filter(flags)(_.isPresentZIO(args))
      _            <- ZIO
                        .cond(!conflictingFlags(presentFlags), (), new Exception())
                        .tapError(_ =>
                          Console.printLine("You have used two conflicting flags.") *>
                            Console.printLine(s"> ${args.mkString(" ")}")
                        )
      _            <- ZIO
                        .cond(!missingRequiredFlags(presentFlags), (), new Exception())
                        .tapError(_ =>
                          Console.printLine("Missing a required flag.") *>
                            Console.printLine(s"> ${args.mkString(" ")}")
                        )

      _ <- action(args).unit
    } yield ()
  }.catchAll(_ => printHelp)

}

trait UnitCommand extends Command[Unit]
