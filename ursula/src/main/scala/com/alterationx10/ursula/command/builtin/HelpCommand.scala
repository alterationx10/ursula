package com.alterationx10.ursula.command.builtin

import com.alterationx10.ursula.args.{Argument, Flag}
import com.alterationx10.ursula.args.builtin.HelpFlag
import com.alterationx10.ursula.command.Command
import com.alterationx10.ursula.services.TTY
import zio.*

case class HelpCommand(commands: Seq[Command[?]], isDefault: Boolean)
    extends Command[Unit] {

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

  override val flags: Seq[Flag[?]] = Seq(
    HelpFlag
  )

  override val arguments: Seq[Argument[?]] = Seq.empty

  override def action(args: Chunk[String]): Task[Unit] = for {
    _ <- TTY.printLine("The CLI supports the following commands:")
    _ <- ZIO.foreach(commands.filter(!_.hidden))(c =>
           TTY.printLine(s"${c.trigger}: ${c.description}")
         )
    _ <- ZIO.when(!this.hidden)(
           TTY.printLine(s"${this.trigger}: ${this.description}")
         )
    _ <-
      TTY.printLine(
        s"use [cmd] ${HelpFlag._sk}, ${HelpFlag._lk} for cmd-specific help"
      )
  } yield ()

}
