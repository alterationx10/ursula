package ursula

import ursula.command.Command
import ursula.command.builtin.HelpCommand

import scala.util.*

trait UrsulaApp {

  /** This setting determines whether the built-in HelpCommand is the default
    * command. Defaults true, override to false if you want to use a different
    * Command as default.
    */
  val defaultHelp: Boolean = true

  /** This is a Seq of your Command implementations that you want your CLI to
    * have access to.
    */
  val commands: Seq[Command]

  /** A collection of built-in Commands that are always available to the CLI.
    */
  private lazy val builtInCommands: Seq[Command] = Seq(
    HelpCommand(commands = commands, isDefault = defaultHelp)
  )

  /** Provided and built-in commands combined */
  private lazy val _allCommands = commands ++ builtInCommands

  /** A map of trigger -> Command for quick lookup */
  private lazy val commandMap: Map[String, Command] =
    _allCommands.groupBy(_.trigger).map { case (k, v) =>
      k -> v.head
    }

  /** The default command to run if no trigger is found */
  private lazy val findDefaultCommand: Option[Command] = {
    val default = _allCommands.filter(_.isDefaultCommand)
    default.headOption
  }

  final def main(args: Array[String]): Unit = {
    val result: Try[Unit] = args.headOption.flatMap(commandMap.get) match {
      case Some(cmd) =>
        cmd.tryAction(args.drop(1).toIndexedSeq)
      case None =>
        findDefaultCommand match {
          case Some(cmd) => cmd.tryAction(args.toIndexedSeq)
          case None =>
            Failure(
              new IllegalArgumentException(
                "Could not find command from argument, and no default command provided"
              )
            )
        }
    }

    result match {
      case Success(_)         => System.exit(0)
      case Failure(exception) =>
        println(exception.getMessage)
        System.exit(1)
    }
  }

}
