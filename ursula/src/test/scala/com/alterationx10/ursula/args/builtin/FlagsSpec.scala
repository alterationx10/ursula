package com.alterationx10.ursula.args.builtin

import zio.*
import zio.test.*
import zio.test.Assertion.*
import com.alterationx10.ursula.args.builtin.HelpFlag

object FlagsSpec extends ZIOSpecDefault {

  def toChunk(str: String): Chunk[String] =
    Chunk.fromArray(str.split(" "))

  val presentArgs: Seq[Chunk[String]] = Seq(
    "-h",
    "--help",
    "command -h",
    "command --help",
    "command arg1 arg2 --help"
  ).map(toChunk)

  val missingArgs: Seq[Chunk[String]] = Seq(
    "command h help arg1 arg2",
    "arg1 arg2"
  ).map(toChunk)

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("FlagsSpec")(
      suite("HelpFlag")(
        test("name")(
          assert(HelpFlag.name)(equalTo("help"))
        ),
        test("shortKey")(
          assert(HelpFlag.shortKey)(equalTo("h"))
        ),
        test("isBoolean")(
          assert(HelpFlag.expectsArgument)(isFalse)
        ),
        test("no conflicts")(
          assert(HelpFlag.exclusive)(isNone)
        ),
        test("no dependencies")(
          assert(HelpFlag.dependsOn)(isNone)
        ),
        test("isPresent")(
          for {
            p <- ZIO.foreach(presentArgs)(HelpFlag.isPresentZIO)
            m <- ZIO.foreach(missingArgs)(HelpFlag.isPresentZIO)
          } yield assertTrue(
            p.forall(_ == true),
            m.forall(_ == false)
          )
        )
      )
    )

}
