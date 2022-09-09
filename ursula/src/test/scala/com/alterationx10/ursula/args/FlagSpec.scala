//package com.alterationx10.ursula.args
//
//import com.alterationx10.ursula.extensions.*
//import zio.*
//import zio.test.*
//
//object FlagSpec extends ZIOSpecDefault {
//
//  // NOTE: sbt provides TEST_FLAG in the env. Test run without sbt, i.e.
//  // in VSCode will fail, without additional set up.
//
//  trait TestFlag extends StringFlag {
//    override val description: String = "A flag for testing"
//    override val name: String        = "override"
//    override val shortKey: String    = "t"
//  }
//
//  // No def, no env
//  case object TestFlag extends TestFlag
//
//  // No def, env present
//  case object TestFlagEnv extends TestFlag {
//    override val env: Option[String] = Option("TEST_FLAG")
//  }
//
//  // No def, env not present
//  case object TestFlagEnv2 extends TestFlag {
//    override val env: Option[String] = Option("NOT_PRESENT")
//  }
//
//  // def, no env
//  case object TestFlagDef extends TestFlag {
//    override val default: Option[String] = Option("123")
//  }
//
//  // Has both def, env
//  case object TestFlagEnvDef extends TestFlag {
//    override val default: Option[String] = Option("123")
//    override val env: Option[String]     = Option("TEST_FLAG")
//
//  }
//
//  // Has both def, env - env not present
//  case object TestFlagEnvDef2 extends TestFlag {
//    override val default: Option[String] = Option("123")
//    override val env: Option[String]     = Option("NOT_PRESENT")
//
//  }
//
//  val argsWithFlag: Chunk[String] = "-l this -l that -t xyz".chunked
//  val argsNoFlag: Chunk[String]   = "-l this -l that".chunked
//
//  override def spec: Spec[TestEnvironment & Scope, Any] =
//    suite("FlagSpec")(
//      suite("parseFirstArg")(
//        test("provided from CLI")(
//          for {
//            a <- TestFlag.parseFirstArgZIO(argsWithFlag)
//            b <- TestFlagEnv.parseFirstArgZIO(argsWithFlag)
//            c <- TestFlagEnv2.parseFirstArgZIO(argsWithFlag)
//            d <- TestFlagDef.parseFirstArgZIO(argsWithFlag)
//            e <- TestFlagEnvDef.parseFirstArgZIO(argsWithFlag)
//            f <- TestFlagEnvDef2.parseFirstArgZIO(argsWithFlag)
//          } yield assertTrue(
//            a.exists(_ == "xyz"),
//            b == a,
//            c == a,
//            d == a,
//            e == a,
//            f == a
//          )
//        ),
//        test("provided from default/env")(
//          for {
//            a <- TestFlag.parseFirstArgZIO(argsNoFlag)
//            b <- TestFlagEnv.parseFirstArgZIO(argsNoFlag)
//            c <- TestFlagEnv2.parseFirstArgZIO(argsNoFlag)
//            d <- TestFlagDef.parseFirstArgZIO(argsNoFlag)
//            e <- TestFlagEnvDef.parseFirstArgZIO(argsNoFlag)
//            f <- TestFlagEnvDef2.parseFirstArgZIO(argsNoFlag)
//          } yield assertTrue(
//            a.isEmpty,
//            b.exists(_ == "abc"),
//            c.isEmpty,
//            d.exists(_ == "123"),
//            e.exists(_ == "abc"),
//            f.exists(_ == "123")
//          )
//        )
//      ),
//      suite("parseArgs")(
//        test("provided from CLI")(
//          for {
//            a <- TestFlag.parseArgsZIO(argsWithFlag)
//            b <- TestFlagEnv.parseArgsZIO(argsWithFlag)
//            c <- TestFlagEnv2.parseArgsZIO(argsWithFlag)
//            d <- TestFlagDef.parseArgsZIO(argsWithFlag)
//            e <- TestFlagEnvDef.parseArgsZIO(argsWithFlag)
//            f <- TestFlagEnvDef2.parseArgsZIO(argsWithFlag)
//          } yield assertTrue(
//            a.exists(_ == "xyz"),
//            b == a,
//            c == a,
//            d == a,
//            e == a,
//            f == a
//          )
//        ),
//        test("provided from default/env")(
//          for {
//            a <- TestFlag.parseArgsZIO(argsNoFlag)
//            b <- TestFlagEnv.parseArgsZIO(argsNoFlag)
//            c <- TestFlagEnv2.parseArgsZIO(argsNoFlag)
//            d <- TestFlagDef.parseArgsZIO(argsNoFlag)
//            e <- TestFlagEnvDef.parseArgsZIO(argsNoFlag)
//            f <- TestFlagEnvDef2.parseArgsZIO(argsNoFlag)
//          } yield assertTrue(
//            a.isEmpty,
//            b.exists(_ == "abc"),
//            c.isEmpty,
//            d.exists(_ == "123"),
//            e.exists(_ == "abc"),
//            f.exists(_ == "123")
//          )
//        )
//      )
//    )
//
//}
