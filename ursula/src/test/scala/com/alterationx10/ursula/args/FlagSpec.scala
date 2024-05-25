package com.alterationx10.ursula.args

import com.alterationx10.ursula.extensions.*
import com.alterationx10.ursula.services.{Config, ConfigLive}
import utest.*
import zio.*

object FlagSpec extends TestSuite with UrsulaTestExtensions {

  // NOTE: This relies on teh contents .env.test to be loaded
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

  val argsWithFlag: Chunk[String] = "-l this -l that -t xyz".chunked
  val argsNoFlag: Chunk[String]   = "-l this -l that".chunked

  implicit val rt: Runtime.Scoped[Config] =
    ConfigLive.temp.testRuntime
  override def tests: Tests               = Tests {
    test("parseFirstArgZIO") {
      test("cli") {
        val a = TestFlag.parseFirstArgZIO(argsWithFlag).testValue
        val b = TestFlagEnv.parseFirstArgZIO(argsWithFlag).testValue
        val c = TestFlagEnv2.parseFirstArgZIO(argsWithFlag).testValue
        val d = TestFlagDef.parseFirstArgZIO(argsWithFlag).testValue
        val e = TestFlagEnvDef.parseFirstArgZIO(argsWithFlag).testValue
        val f = TestFlagEnvDef2.parseFirstArgZIO(argsWithFlag).testValue
        assert(
          a.contains("xyz"),
          b == a,
          c == a,
          d == a,
          e == a,
          f == a
        )
      }
      test("default") {
        val a = TestFlag.parseFirstArgZIO(argsNoFlag).testValue
        val b = TestFlagEnv.parseFirstArgZIO(argsNoFlag).testValue
        val c = TestFlagEnv2.parseFirstArgZIO(argsNoFlag).testValue
        val d = TestFlagDef.parseFirstArgZIO(argsNoFlag).testValue
        val e = TestFlagEnvDef.parseFirstArgZIO(argsNoFlag).testValue
        val f = TestFlagEnvDef2.parseFirstArgZIO(argsNoFlag).testValue
        assert(
          a.isEmpty,
          b.contains("abc"),
          c.isEmpty,
          d.contains("123"),
          e.contains("abc"),
          f.contains("123")
        )
      }
    }

    test("parseArgsZIO") {
      test("cli") {
        val a = TestFlag.parseArgsZIO(argsWithFlag).testValue
        val b = TestFlagEnv.parseArgsZIO(argsWithFlag).testValue
        val c = TestFlagEnv2.parseArgsZIO(argsWithFlag).testValue
        val d = TestFlagDef.parseArgsZIO(argsWithFlag).testValue
        val e = TestFlagEnvDef.parseArgsZIO(argsWithFlag).testValue
        val f = TestFlagEnvDef2.parseArgsZIO(argsWithFlag).testValue
        assert(
          a.contains("xyz"),
          b == a,
          c == a,
          d == a,
          e == a,
          f == a
        )
      }
      test("default") {
        val a = TestFlag.parseArgsZIO(argsNoFlag).testValue
        val b = TestFlagEnv.parseArgsZIO(argsNoFlag).testValue
        val c = TestFlagEnv2.parseArgsZIO(argsNoFlag).testValue
        val d = TestFlagDef.parseArgsZIO(argsNoFlag).testValue
        val e = TestFlagEnvDef.parseArgsZIO(argsNoFlag).testValue
        val f = TestFlagEnvDef2.parseArgsZIO(argsNoFlag).testValue
        assert(
          a.isEmpty,
          b.contains("abc"),
          c.isEmpty,
          d.contains("123"),
          e.contains("abc"),
          f.contains("123")
        )
      }
    }
  }

}
