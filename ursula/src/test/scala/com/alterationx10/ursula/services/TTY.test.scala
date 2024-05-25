package com.alterationx10.ursula.services

import com.alterationx10.ursula.extensions.UrsulaTestExtensions
import utest.*
import zio.{Runtime, Scope, ZLayer, ZIO}

import java.io.{ByteArrayOutputStream, StringReader}

object TTYSpec extends TestSuite with UrsulaTestExtensions {

  implicit val rt: Runtime.Scoped[Config] =
    ConfigLive.temp.testRuntime

  override def tests: Tests = Tests {
    test("readLine") {

      val result = scala.Console.withIn(new StringReader("abc")) {
        TTY.readLine.testValue == "abc"
      }
      assert(result)
    }

    test("print") {
      val out = new ByteArrayOutputStream()
      scala.Console.withOut(out) {
        TTY.print("abc").testValue
      }
      assert(out.toString == "abc")
    }

    test("printError") {
      val out = new ByteArrayOutputStream()
      scala.Console.withOut(out) {
        TTY.printError("abc").testValue
      }
      assert(out.toString == "abc")
    }

    test("printLibe") {
      val out = new ByteArrayOutputStream()
      scala.Console.withOut(out) {
        TTY.printLine("abc").testValue
      }
      assert(out.toString == "abc\n")
    }

    test("printLineError") {
      val out = new ByteArrayOutputStream()
      scala.Console.withOut(out) {
        TTY.printLineError("abc").testValue
      }
      assert(out.toString == "abc\n")
    }

  }
}
