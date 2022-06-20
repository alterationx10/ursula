package com.alterationx10.ursula.command.builtin

import com.alterationx10.ursula.command.Command
import com.alterationx10.ursula.args.Argument
import com.alterationx10.ursula.args.Flag
import zio.Chunk
import zio.Task

object ConfigCommand extends Command[Unit]{

  override val description: String = ???

  override val trigger: String = ???

  override def action(args: Chunk[String]): Task[Unit] = ???

  override val examples: Seq[String] = ???

  override val flags: Seq[Flag[?]] = ???

  override val usage: String = ???

  override val arguments: Seq[Argument[?]] = ???

  
}
