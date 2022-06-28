package com.alterationx10.example.commands

import com.alterationx10.ursula.args.{Argument, Flag}
import com.alterationx10.ursula.command.Command

import zio.*

case object SarcasticFlag extends Flag[Unit] {

  override val name: String = "sarcastic"

  override val shortKey: String = "s"

  override val description: String = "prints the argument in alternating case"

  override val expectsArgument: Boolean = false

  override def parse: PartialFunction[String, Unit] = _ => ()

  override val exclusive: Option[Seq[Flag[?]]] = Some(Seq(LoudFlag))

}

case object LoudFlag extends Flag[Unit] {

  override val name: String = "loud"

  override val shortKey: String = "l"

  override val description: String = "converts to uppercase"

  override val expectsArgument: Boolean = false

  override def parse: PartialFunction[String, Unit] = _ => ()

  override val exclusive: Option[Seq[Flag[?]]] = Some(Seq(SarcasticFlag))

}

final case class Echo() extends Command[Unit] {

  override val description: String = "Echoes back the provided argument"

  override val usage: String = "echo [?flag] some stuff to print back"

  override val examples: Seq[String] = Seq(
    "echo hello",
    "echo -s sarcasm is hard to convey on the internet",
    "echo -l I said good day"
  )

  override val trigger: String = "echo"

  override val flags: Seq[Flag[?]] = Seq(
    LoudFlag,
    SarcasticFlag
  )

  override val arguments: Seq[Argument[?]] = Seq.empty

  // LOUDLY
  val loudly: String => String =
    str => str.toUpperCase()

  // SaRcAsM
  val sarcastically: String => String =
    str =>
      str.zipWithIndex.map {
        case odd if odd._2 % 2 == 0 => odd._1.toString.toUpperCase()
        case even => even._1.toString.toLowerCase()
      }.mkString

  override def action(
      args: Chunk[String]
  ): ZIO[UrsulaServices, Throwable, Unit] = for {
    argString <- ZIO.attempt(stripFlags(args).mkString(" "))
    _         <- Console
                   .printLine(loudly(argString))
                   .when(LoudFlag.isPresent(args))
    _         <- Console
                   .printLine(sarcastically(argString))
                   .when(SarcasticFlag.isPresent(args))
    _         <- Console
                   .printLine(argString)
                   .when(!LoudFlag.isPresent(args) && !SarcasticFlag.isPresent(args))
  } yield ()

}
