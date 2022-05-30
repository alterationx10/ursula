package com.alterationx10.example.commands

import com.alterationx10.ursula.args.{Argument, Flag}
import com.alterationx10.ursula.command.Command

import zio.*

final case class Echo() extends Command[String] {

  val loudFlag: Flag[String] = new Flag[String] {

    override val name: String = "loud"

    override val shortKey: String = "l"

    override val description: String = "converts to argument to uppercase"

    override def parse: PartialFunction[String, String] =
      str => loudly(str)

  }

  val sarcasticFlag: Flag[String] = new Flag[String] {

    override val name: String = "sarcastic"

    override val shortKey: String = "s"

    override val description: String = "prints the argument in alternating case"

    override def parse: PartialFunction[String, String] =
      str => sarcastically(str)
  }

  override val description: String = "Echoes back the provided argument"

  override val usage: String = """echo "some stuff""""

  override val examples: Seq[String] = Seq(
    "echo hello",
    "echo -s hello",
    """echo -l "good day""""
  )

  override val trigger: String = "echo"

  override val flags: Seq[Flag[?]] = Seq(
    loudFlag,
    sarcasticFlag
  )

  override val arguments: Seq[Argument[?]] = Seq.empty

  val sarcastically: String => String =
    str =>
      str.zipWithIndex.map {
        case odd if odd._2 % 2 == 0 => odd._1.toString.toUpperCase()
        case even => even._1.toString.toLowerCase()
      }.mkString

  val loudly: String => String =
    str => str.toUpperCase()

  override def action(args: Chunk[String]): Task[String] = for {
    lArg <- loudFlag.parseFirstArgZIO(args)
    sArg <- sarcasticFlag.parseFirstArgZIO(args)
    str  <- ZIO
              .fromOption(lArg)
              .catchAll(_ => ZIO.fromOption(sArg))
              .catchAll(_ => ZIO.fromOption(args.drop(1).headOption))
              .catchAll(_ => ZIO.succeed(""))
    _    <- Console.printLine(str)
  } yield str

}
