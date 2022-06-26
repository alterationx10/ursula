package com.alterationx10.ursula.command

import zio.*
import zio.test.*
import com.alterationx10.ursula.args.Flag
import com.alterationx10.ursula.args.BooleanFlag
import com.alterationx10.ursula.args.StringFlag
import com.alterationx10.ursula.errors.MissingFlagsException
import com.alterationx10.ursula.errors.ConflictingFlagsException
import com.alterationx10.ursula.errors.UnrecognizedFlagException
import com.alterationx10.ursula.extensions.*
import com.alterationx10.ursula.services.config.UrsulaConfig
import com.alterationx10.ursula.services.config.UrsulaConfigLive

// A <-> B Conflict
// C requires an argument
// D is a required flag

object CommandSpec extends ZIOSpecDefault {

  case object AFlag extends BooleanFlag {
    override val description: String = "A flag"
    override val name: String        = "aaa"
    override val shortKey: String    = "a"

    override val exclusive: Option[Seq[Flag[?]]] = Some(Seq(BFlag))

  }

  case object BFlag extends BooleanFlag {
    override val description: String = "B flag"
    override val name: String        = "bbb"
    override val shortKey: String    = "b"

    override val exclusive: Option[Seq[Flag[?]]] = Some(Seq(AFlag))
  }

  case object CFlag extends StringFlag {
    override val description: String = "C flag"
    override val name: String        = "ccc"
    override val shortKey: String    = "c"

  }

  case object DFlag extends BooleanFlag {
    override val description: String = "D flag"
    override val name: String        = "ddd"
    override val shortKey: String    = "d"
    override val required: Boolean   = true

  }

  case object TestCommand extends UnitCommand {
    override def action(args: Chunk[String]): ZIO[UrsulaServices, Throwable, Unit] = ZIO.unit

    val arguments: Seq[com.alterationx10.ursula.args.Argument[?]] = Seq.empty
    val description: String                                       = ""
    val examples: Seq[String]                                     = Seq.empty
    val flags: Seq[com.alterationx10.ursula.args.Flag[?]]         =
      Seq(AFlag, BFlag, CFlag, DFlag)
    val trigger: String                                           = "test"
    val usage: String                                             = "Used in a test"
  }

  val goodCommand: Chunk[String] = "-a -d -c 123".chunked
  val missingFlag: Chunk[String] = "-c 123".chunked
  val unknownArg: Chunk[String]  = "-f".chunked
  val conflicting: Chunk[String] = "-a -b -d".chunked
  val help: Chunk[String]        = "-a -b -d -f -h".chunked

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CommandSpec")(
      test("should succeed when flags are given correctly")(
        for {
          _ <- TestCommand.processedAction(goodCommand)
        } yield assertCompletes
      ),
      test("should succeed when help flag is given")(
        for {
          _ <- TestCommand.processedAction(help)
        } yield assertCompletes
      ),
      test("should fail when missing a required flag")(
        for {
          err <- TestCommand.processedAction(missingFlag).flip
        } yield assertTrue(err == MissingFlagsException)
      ),
      test("should fail when missing an expected arg for a flag")(
        for {
          err <- TestCommand.processedAction(unknownArg).flip
        } yield assertTrue(err == UnrecognizedFlagException)
      ),
      test("should fail when given conflicting flags")(
        for {
          err <- TestCommand.processedAction(conflicting).flip
        } yield assertTrue(err == ConflictingFlagsException)
      )
    ).provideCustomLayer(UrsulaConfigLive.live)

}
