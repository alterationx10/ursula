package com.alterationx10.ursula.args.builtin

import zio.test._
import zio.test.Assertion._
import zio._

object FlagsSpec extends ZIOSpecDefault {

  import Flags._

  def toChunk(str: String): Chunk[String] =
    Chunk.fromArray(str.split(" "))

  val presentArgs: Seq[Chunk[String]] = Seq(
    "-h",
    "--help",
    "command -h",
    "command --help",
    "command arg1 arg2 --help",
  ).map(toChunk)

  val missingArgs : Seq[Chunk[String]] = Seq(
    "command h help arg1 arg2",
    "arg1 arg2"
  ).map(toChunk)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FlagsSpec")(
      suite("helpFlag")(
        test("name")(
          assert(helpFlag.name)(equalTo("help"))
        ),
        test("shortKey")(
          assert(helpFlag.shortKey)(equalTo("h"))
        ),
        test("isBoolean")(
          assert(helpFlag.expectsArgument)(isFalse)
        ),
        test("no conflicts")(
          assert(helpFlag.exclusive)(isNone)
        ),
        test("no dependencies")(
          assert(helpFlag.dependsOn)(isNone)
        ),
        test("isPresent")(
          for {
            p <- ZIO.foreach(presentArgs)(helpFlag.isPresentZIO)
            m <- ZIO.foreach(missingArgs)(helpFlag.isPresentZIO)
          } yield assertTrue(
            p.forall(_ == true),
            m.forall(_ == false)
          )
        )
      )
    )

}
