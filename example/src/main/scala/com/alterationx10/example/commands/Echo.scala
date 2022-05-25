package com.alterationx10.example.commands

import com.alterationx10.ursula.args.{ArgParsers, Argument, Flag}
import com.alterationx10.ursula.command.Command
import zio._

final case class Echo() extends Command[String] with ArgParsers {

  val loudFlag: Flag      =
    Flag("l", "loud", "returns ALL CAPS", isArgument = true)
  val sarcasticFlag: Flag =
    Flag("s", "sarcastic", "returns SaRcAsM", isArgument = true)

  override val description: String = "Echoes back the provided argument"

  override val usage: String = """echo "some stuff""""

  override val examples: Seq[String] = Seq(
    "echo hello",
    "echo -s hello",
    """echo -l "good day""""
  )

  override val trigger: String = "echo"

  override val flags: Seq[Flag] = Seq(
    loudFlag,
    sarcasticFlag
  )

  override val arguments: Seq[Argument] = Seq.empty

  val sarcastically: String => String =
    str =>
      str.zipWithIndex.map {
        case odd if odd._2 % 2 == 0 => odd._1.toString().toUpperCase()
        case even => even._1.toString().toLowerCase()
      }.mkString

  val loudly: String => String =
    str => str.toUpperCase()

  override def action(args: Chunk[String]): Task[String] = for {
    lArg <- ZIO.attempt(
              getFlagArgument[String](args)(loudFlag)(loudly)
            )
    sArg <- ZIO.attempt(
              getFlagArgument[String](args)(sarcasticFlag)(sarcastically)
            )
    str  <- ZIO
              .fromOption(lArg)
              .catchAll(_ => ZIO.fromOption(sArg))
              .catchAll(_ => ZIO.fromOption(args.drop(1).headOption))
              .catchAll(_ => ZIO.succeed(""))
    _    <- Console.printLine(str)
  } yield str

}
