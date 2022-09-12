package com.alterationx10.ursula.args.builtin

import com.alterationx10.ursula.extensions.UrsulaTestExtensions
import com.alterationx10.ursula.services.{Config, ConfigLive}
import utest.*
import zio.*

object FlagsSpec extends TestSuite with UrsulaTestExtensions {

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

  implicit val rt: Runtime.Scoped[Config] =
    ConfigLive.temp.testRuntime

  override def tests: Tests = Tests {
    test("name") {
      assert(HelpFlag.name == "help")
    }
    test("shortKey") {
      assert(HelpFlag.shortKey == "h")
    }
    test("isBoolean") {
      assert(!HelpFlag.expectsArgument)
    }
    test("no conflicts") {
      assert(HelpFlag.exclusive.isEmpty)
    }
    test("do dependencies") {
      assert(HelpFlag.dependsOn.isEmpty)
    }
    test("is present") {
      val result: ZIO[Any, Throwable, Boolean] = for {
        p <- ZIO.foreach(presentArgs)(HelpFlag.isPresentZIO)
        m <- ZIO.foreach(missingArgs)(HelpFlag.isPresentZIO)
      } yield p.forall(_ == true) &&
        m.forall(_ == false)

      assert(result.expect(_ == true))
    }
  }

}
