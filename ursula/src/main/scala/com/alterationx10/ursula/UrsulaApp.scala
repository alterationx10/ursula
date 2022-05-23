package com.alterationx10.ursula

import zio._
import com.alterationx10.ursula.command.Command
import com.alterationx10.ursula.command.builtin.HelpCommand

trait UrsulaApp extends ZIOAppDefault {

  val defaultHelp: Boolean = true
  val commandLayer: ZLayer[Any, Nothing, Seq[Command[_]]]

  private lazy val withBuiltIns: ZLayer[Any, Nothing, Seq[Command[_]]] =
    commandLayer >>> ZLayer.fromZIO {
      for {
        commands <- ZIO.service[Seq[Command[_]]]
      } yield Seq(
        HelpCommand(commands, defaultHelp)
      ) ++ commands
    }

  private final def getDefaultCommand(
      commands: Seq[Command[_]]
  ): ZIO[Any, Nothing, Option[Command[_]]] = for {
    _ <- ZIO
      .when(commands.filter(_.isDefaultCommand).size > 1)(
        ZIO.logWarning(
          "Multiple commands are flagged as Default! Using first provided..."
        )
      )
  } yield commands.find(_.isDefaultCommand)

  private final def getTriggerCommand(
      args: Chunk[String],
      commands: Seq[Command[_]]
  ): Option[Command[_]] =
    args.headOption.flatMap(t => commands.find(_.trigger == t))

  private final val program
      : ZIO[Seq[Command[_]] with ZIOAppArgs, Throwable, ExitCode] =
    for {
      args <- ZIOAppArgs.getArgs
      commands <- ZIO.service[Seq[Command[_]]]
      cmd <- ZIO
        .fromOption(getTriggerCommand(args, commands))
        .catchAll(_ =>
          getDefaultCommand(commands).someOrFail(
            new Exception(
              "Could not find command from argument, and no default command provided"
            )
          )
        )
      _ <- cmd.processedAction(args)
    } yield ExitCode.success

  override final def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    program.provideSome[ZIOAppArgs](withBuiltIns)

}

object MuhApp extends UrsulaApp {

  override val commandLayer: ZLayer[Any, Nothing, Seq[Command[_]]] =
    ZLayer.succeed(Seq.empty)

}
