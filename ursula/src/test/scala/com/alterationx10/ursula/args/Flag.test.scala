package com.alterationx10.ursula.args

import com.alterationx10.ursula.extensions.*
import com.alterationx10.ursula.services.{CliConfig, CliConfigLive}
import com.alterationx10.ursula.errors.*
import zio.*
import zio.test.*

object FlagSpec extends ZIOSpecDefault {

  // NOTE: This relies on the contents .env to be loaded
  trait TestFlag extends StringFlag {
    override val description: String = "A flag for testing"
    override val name: String        = "override"
    override val shortKey: String    = "t"
  }

  // No def, no env
  case object TestFlag extends TestFlag

  // No def, env present
  case object TestFlagEnv extends TestFlag {
    override val env: Option[String] = Option("TEST_FLAG")
  }

  // No def, env not present
  case object TestFlagEnv2 extends TestFlag {
    override val env: Option[String] = Option("NOT_PRESENT")
  }

  // def, no env
  case object TestFlagDef extends TestFlag {
    override val default: Option[String] = Option("123")
  }

  // Has both def, env
  case object TestFlagEnvDef extends TestFlag {
    override val default: Option[String] = Option("123")
    override val env: Option[String]     = Option("TEST_FLAG")

  }

  // Has both def, env - env not present
  case object TestFlagEnvDef2 extends TestFlag {
    override val default: Option[String] = Option("123")
    override val env: Option[String]     = Option("NOT_PRESENT")

  }

  case object TestFlagOptions extends TestFlag {
    override val options: Option[Set[String]] = Option(Set("abc", "123"))
  }

  case object TestFlagOptionsEnv extends TestFlag {
    override val options: Option[Set[String]] = Option(Set("abc", "123"))
    override val env: Option[String]          = Option("BAD_OPTION_TEST_FLAG")
  }

  case object TestFlagOptionsDef extends TestFlag {
    override val options: Option[Set[String]] = Option(Set("abc", "123"))
    override val default: Option[String]      = Some("xyz")
  }

  val argsWithFlag: Chunk[String] = "-l this -l that -t xyz".chunked
  val argsNoFlag: Chunk[String]   = "-l this -l that".chunked

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("FlagSpec")(
      suite("parseFirstArgZIO")(
        test("cli") {
          for {
            a <- TestFlag.parseFirstArgZIO(argsWithFlag)
            b <- TestFlagEnv.parseFirstArgZIO(argsWithFlag)
            c <- TestFlagEnv2.parseFirstArgZIO(argsWithFlag)
            d <- TestFlagDef.parseFirstArgZIO(argsWithFlag)
            e <- TestFlagEnvDef.parseFirstArgZIO(argsWithFlag)
            f <- TestFlagEnvDef2.parseFirstArgZIO(argsWithFlag)
          } yield assertTrue(
            a.contains("xyz"),
            b == a,
            c == a,
            d == a,
            e == a,
            f == a
          )
        },
        test("default") {
          for {
            a <- TestFlag.parseFirstArgZIO(argsNoFlag)
            b <- TestFlagEnv.parseFirstArgZIO(argsNoFlag)
            c <- TestFlagEnv2.parseFirstArgZIO(argsNoFlag)
            d <- TestFlagDef.parseFirstArgZIO(argsNoFlag)
            e <- TestFlagEnvDef.parseFirstArgZIO(argsNoFlag)
            f <- TestFlagEnvDef2.parseFirstArgZIO(argsNoFlag)
          } yield assertTrue(
            a.isEmpty,
            b.contains("abc"),
            c.isEmpty,
            d.contains("123"),
            e.contains("abc"),
            f.contains("123")
          )
        }
      ),
      suite("parseArgsZIO")(
        test("cli") {
          for {
            a <- TestFlag.parseArgsZIO(argsWithFlag)
            b <- TestFlagEnv.parseArgsZIO(argsWithFlag)
            c <- TestFlagEnv2.parseArgsZIO(argsWithFlag)
            d <- TestFlagDef.parseArgsZIO(argsWithFlag)
            e <- TestFlagEnvDef.parseArgsZIO(argsWithFlag)
            f <- TestFlagEnvDef2.parseArgsZIO(argsWithFlag)
          } yield assertTrue(
            a.contains("xyz"),
            b == a,
            c == a,
            d == a,
            e == a,
            f == a
          )
        },
        test("default") {
          for {
            a <- TestFlag.parseArgsZIO(argsNoFlag)
            b <- TestFlagEnv.parseArgsZIO(argsNoFlag)
            c <- TestFlagEnv2.parseArgsZIO(argsNoFlag)
            d <- TestFlagDef.parseArgsZIO(argsNoFlag)
            e <- TestFlagEnvDef.parseArgsZIO(argsNoFlag)
            f <- TestFlagEnvDef2.parseArgsZIO(argsNoFlag)
          } yield assertTrue(
            a.isEmpty,
            b.contains("abc"),
            c.isEmpty,
            d.contains("123"),
            e.contains("abc"),
            f.contains("123")
          )
        }
      ),
      suite("only accepts valid options")(
        test("cli") {
          for {
            _     <- TestFlagOptions.parseArgsZIO("-t 123 -t abc".chunked)
            empty <- TestFlagOptions.parseArgsZIO(Chunk.empty)
            err   <- TestFlagOptions.parseArgsZIO("-t xyz".chunked).flip
          } yield assertTrue(
            empty.isEmpty,
            err == InvalidOptionFlagException
          )
        },
        test("env") {
          for {
            a <- TestFlagOptionsEnv.parseArgsZIO("-t 123 -t abc".chunked)
            e <- TestFlagOptionsEnv.parseArgsZIO(Chunk.empty).flip
          } yield assertTrue(
            a.contains("123"),
            a.contains("abc"),
            e == InvalidOptionFlagException
          )
        },
        test("default") {
          for {
            a <-
              TestFlagOptionsDef.parseArgsZIO("-l this -l that -t 123".chunked)
            e <- TestFlagOptionsDef.parseArgsZIO(Chunk.empty).flip
          } yield assertTrue(
            a.contains("123"),
            e == InvalidOptionFlagException
          )
        }
      )
    )

}
