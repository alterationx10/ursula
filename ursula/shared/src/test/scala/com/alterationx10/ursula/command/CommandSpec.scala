package com.alterationx10.ursula.command

import zio.*
import com.alterationx10.ursula.args.Flag
import com.alterationx10.ursula.args.BooleanFlag
import com.alterationx10.ursula.args.StringFlag
import com.alterationx10.ursula.errors.MissingFlagsException
import com.alterationx10.ursula.errors.ConflictingFlagsException
import com.alterationx10.ursula.errors.UnrecognizedFlagException
import com.alterationx10.ursula.extensions.*
import com.alterationx10.ursula.services.{Config, ConfigLive}
import utest.*

// A <-> B Conflict
// C requires an argument
// D is a required flag

object CommandSpec extends TestSuite with UrsulaTestExtensions {

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

  trait TestCommand extends UnitCommand {
    override def action(
        args: Chunk[String]
    ): ZIO[UrsulaServices, Throwable, Unit] = ZIO.unit

    val arguments: Seq[com.alterationx10.ursula.args.Argument[?]] = Seq.empty
    val description: String                                       = ""
    val examples: Seq[String]                                     = Seq.empty
    val flags: Seq[com.alterationx10.ursula.args.Flag[?]]         =
      Seq(AFlag, BFlag, CFlag, DFlag)
    val trigger: String                                           = "test"
    val usage: String                                             = "Used in a test"
  }

  case object TestCommand extends TestCommand

  case object NonStrictTestCommand extends TestCommand {
    override val strict: Boolean = false
  }

  val goodCommand: Chunk[String] = "-a -d -c 123".chunked
  val missingFlag: Chunk[String] = "-c 123".chunked
  val unknownArg: Chunk[String]  = "-f".chunked
  val conflicting: Chunk[String] = "-a -b -d".chunked
  val help: Chunk[String]        = "-a -b -d -f -h".chunked

  implicit val rt: Runtime.Scoped[Config] =
    ConfigLive.temp.testRuntime

  override def tests: Tests = Tests {

    test("should succeed when flags are given correctly") {
      TestCommand.processedAction(goodCommand).testValue
    }

    test("should succeed when help flag is given") {
      TestCommand.processedAction(help).testValue
    }

    test("should fail when missing a required flag") {
      assert(
        TestCommand
          .processedAction(missingFlag)
          .flip
          .testValue == MissingFlagsException
      )
    }

    test("should fail when missing an expected arg for a flag") {

      assert(
        TestCommand
          .processedAction(unknownArg)
          .flip
          .testValue == UnrecognizedFlagException
      )

    }

    test("should fail when given conflicting flags") {
      assert(
        TestCommand
          .processedAction(conflicting)
          .flip
          .testValue == ConflictingFlagsException
      )
    }

    test(
      "should not fail on unknown/conflicting/missing flags if non-strict"
    ) {
      NonStrictTestCommand.processedAction(unknownArg).testValue
      NonStrictTestCommand.processedAction(conflicting).testValue
      NonStrictTestCommand.processedAction(missingFlag).testValue
    }

  }

}
