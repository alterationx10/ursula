package com.alterationx10.ursula.command.builtin

import com.alterationx10.ursula.command.Command
import zio._
import com.alterationx10.ursula.args.ArgParsers
import com.alterationx10.ursula.args.Flag
import com.alterationx10.ursula.args.builtin.Flags
import com.alterationx10.ursula.args.Argument

case class HelpCommand(commands: Seq[Command[_]], isDefault: Boolean)
    extends Command[Unit]
    with ArgParsers {

  override val isDefaultCommand: Boolean = isDefault

  override val description: String =
    "Prints a list of commands, and their description"

  override val usage: String = "help"

  override val examples: Seq[String] = Seq(
    "help",
    "help --help",
    "help -h"
  )

  override val trigger: String = "help"

  override val flags: Seq[Flag] = Seq(
    Flags.helpFlag
  )

  override val arguments: Seq[Argument] = Seq.empty

  override def action(args: Chunk[String]): Task[Unit] = for {
    _ <- Console.printLine("The CLI supports the following commands:")
    _ <- ZIO.foreach(commands.filter(!_.hidden))(c =>
           Console.printLine(s"${c.trigger}: ${c.description}")
         )
    _ <- ZIO.when(!this.hidden)(
           Console.printLine(s"${this.trigger}: ${this.description}")
         )
    _ <-
      Console.printLine(
        s"use [cmd] ${Flags.helpFlag._sk}, ${Flags.helpFlag._lk} for cmd-specific help"
      )
  } yield ()

}
