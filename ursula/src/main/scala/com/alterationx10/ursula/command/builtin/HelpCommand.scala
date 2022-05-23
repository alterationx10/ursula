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

  override val description: String = "Prints this help message"

  override val usage: String = "cmdz help"

  override val examples: Seq[String] = Seq(
    "cmdz help",
    "cmdz --help",
    "cmdz -h"
  )

  override val trigger: String = "help"

  override val flags: Seq[Flag] = Seq(
    Flags.helpFlag
  )
  override val arguments: Seq[Argument] = Seq.empty
  override def action(args: Chunk[String]): Task[Unit] = for {
    _ <- Console.printLine("The CLI supports the following sub-commands:")
    _ <- ZIO.foreach(commands.filter(!_.hidden))(c =>
      Console.printLine(s"${c.trigger}: ${c.description}")
    )
    _ <- Console.printLine(s"use cmdz [cmd] -h || --help for cmd-specific help")
  } yield ()

}
