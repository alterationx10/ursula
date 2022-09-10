package com.alterationx10.ursula.command

import com.alterationx10.ursula.args.{Argument, Flag}
import com.alterationx10.ursula.args.builtin.HelpFlag
import com.alterationx10.ursula.errors.*
import com.alterationx10.ursula.doc.*

import scala.annotation.tailrec
import zio.*
import com.alterationx10.ursula.services.{TTY, UrsulaServices}

trait Command[A] {

  // This type is re-declared to save an import
  type UrsulaServices = UrsulaServices.UrsulaServices

  /** A brief description of what this command does.
    */
  val description: String

  /** A one-line generic example of how to use this command
    */
  val usage: String

  /** Some specific examples of how to use this command
    */
  val examples: Seq[String]

  /** The argument to trigger this command.
    */
  val trigger: String

  /** A collection fo [[com.alterationx10.ursula.args.Flag]] that this command
    * supports.
    */
  val flags: Seq[Flag[?]]

  /** A collection of arguments this command expects.
    */
  val arguments: Seq[Argument[?]]

  /** Hides this command from the help
    */
  val hidden: Boolean = false

  /** Indicates if this command should be the default run, when no other
    * matching trigger is found. There can only be one per CLI.
    */
  val isDefaultCommand: Boolean = false

  /** The central logic to implement for this Command
    * @param args
    *   The cli arguments passed in, with any trigger command already stripped
    *   out.
    * @return
    */
  def action(args: Chunk[String]): ZIO[UrsulaServices, Throwable, A]

  /** Indicates if the program should stop on unrecognized, missing, and/or
    * conflicting flags.
    */
  val strict: Boolean = true

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
          } else if (hasArgumentFlag(h)) {
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

  /** Strips flags and their arguments from the cli arguments, which can then be
    * parsed for Arguments. Wraps
    * [[com.alterationx10.ursula.command.Command.stripFlags]] in a ZIO.attempt
    * @param args
    *   The cli arguments
    */
  def stripFlagsZIO(args: Chunk[String]): Task[Chunk[String]] =
    ZIO.attempt(stripFlags(args))

  /** An object that has formatted help for this command.
    */
  lazy val documentation: Documentation = CommandDoc(this)

  /** Prints documentation
    * @return
    */
  final def printHelp: Task[Unit] =
    TTY.printLine(documentation.txt)

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

  private final def failWhen[E](
      predicate: => Boolean,
      error: E
  ): ZIO[Any, E, Unit] =
    ZIO.cond(!predicate, (), error).when(strict).unit

  private final def printArgs(args: Chunk[String]): Task[Unit] =
    TTY.printLine(s"> ${args.mkString(" ")}")

  private final val printHelpfulError
      : Chunk[String] => CommandException => Task[Unit] =
    args =>
      error =>
        error.printMessageZIO *>
          printArgs(args) *>
          printHelp

  final def processedAction(
      args: Chunk[String]
  ): ZIO[UrsulaServices, Throwable, Unit] = {
    for {
      _            <- failWhen(HelpFlag.isPresent(args), HelpFlagException)
      _            <- failWhen(unrecognizedFlags(args), UnrecognizedFlagException)
                        .tapError { printHelpfulError(args) }
      presentFlags <- ZIO.filter(flags)(_.isPresentZIO(args))
      _            <- failWhen(conflictingFlags(presentFlags), ConflictingFlagsException)
                        .tapError { printHelpfulError(args) }
      _            <- failWhen(missingRequiredFlags(presentFlags), MissingFlagsException)
                        .tapError { printHelpfulError(args) }

      _ <- action(args).unit
    } yield ()
  }.catchSome {
    //
    case HelpFlagException =>
      printHelp
  }

}

trait UnitCommand extends Command[Unit]
