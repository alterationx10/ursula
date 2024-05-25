package com.alterationx10.ursula.services

import com.alterationx10.ursula.extensions.UrsulaTestExtensions
import os.{Path, Source}
import utest.*
import upickle.default.*
import zio.{Runtime, Scope, ZLayer, ZIO}

object ConfigSpec extends TestSuite with UrsulaTestExtensions {

  def rt(path: Path): Runtime.Scoped[Config] = ZLayer
    .make[Config](
      Scope.default,
      ConfigLive
        .live(path.toString().split("/").dropRight(1).mkString("/"), path.last)
    )
    .testRuntime

  override def tests: Tests = Tests {

    test("config doesn't exist") {

      val tempNoExist: Path = os.temp()
      os.remove(tempNoExist)

      val z = for {
        e <- ZIO.serviceWithZIO[Config](_.get(""))
      } yield e.isEmpty

      assert(z.testValue(rt(tempNoExist)))

    }

    test("config with existing data") {

      val srcContent: String =
        writeJs(Map("this" -> "that", "abc" -> "123")).toString()
      val srcData: Source    = Source.WritableSource(srcContent)
      val tempWithData: Path = os.temp(contents = srcData)

      val z = for {
        _this <- ZIO.serviceWithZIO[Config](_.get("this"))
        abc   <- ZIO.serviceWithZIO[Config](_.get("abc"))
      } yield _this.contains("that") && abc.contains("123")

      assert(z.testValue(rt(tempWithData)))

    }

    test("write data") {

      val emptyData: Path = os.temp(contents = Source.WritableSource("{}"))

      val z = for {
        _ <- ZIO.serviceWithZIO[Config](_.set("xyz", "omg"))
      } yield ()

      val runtime = rt(emptyData)
      z.testValue(runtime)
      runtime.shutdown0()

      val setData = os.read(emptyData)

      assert(read[Map[String, String]](setData).get("xyz").contains("omg"))
    }

    test("delete data") {

      val srcContent: String =
        writeJs(Map("this" -> "that")).toString()
      val srcData: Source    = Source.WritableSource(srcContent)
      val tempWithData: Path = os.temp(contents = srcData)

      val z = for {
        _ <- ZIO.serviceWithZIO[Config](_.delete("this"))
      } yield ()

      val _rt = rt(tempWithData)
      z.testValue(_rt)
      _rt.shutdown0()

      val setData = os.read(tempWithData)
      assert(setData == "{}")

    }
  }
}
