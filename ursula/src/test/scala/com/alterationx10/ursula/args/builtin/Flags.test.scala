package com.alterationx10.ursula.args.builtin

import com.alterationx10.ursula.services.{CliConfig, CliConfigLive}
import zio.{Config => ZConfig, *}
import zio.test.*

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
      test("name") {
        assertTrue(HelpFlag.name == "help")
      },
      test("shortKey") {
        assertTrue(HelpFlag.shortKey == "h")
      },
      test("isBoolean") {
        assertTrue(!HelpFlag.expectsArgument)
      },
      test("no conflicts") {
        assertTrue(HelpFlag.exclusive.isEmpty)
      },
      test("do dependencies") {
        assertTrue(HelpFlag.dependsOn.isEmpty)
      },
      test("is present") {
        for {
          p <- ZIO.foreach(presentArgs)(HelpFlag.isPresentZIO)
          m <- ZIO.foreach(missingArgs)(HelpFlag.isPresentZIO)
        } yield assertTrue(
          p.forall(_ == true),
          m.forall(_ == false)
        )
      }
    )

}
