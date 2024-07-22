package com.alterationx10.ursula.services

import com.alterationx10.ursula.extensions.UrsulaTestExtensions
import os.{Path, Source}
import upickle.default.*
import zio.{Runtime, Scope, ZIO, ZLayer}
import zio.test.*

object ConfigSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ConfigSpec")(
      test("config doesn't exist") {
        val tempNoExist: Path = os.temp()
        os.remove(tempNoExist)
        for {
          e <- ZIO
                 .serviceWithZIO[Config](_.get(""))
                 .provide(
                   ConfigLive
                     .live(tempNoExist.toIO.getParent(), tempNoExist.last),
                   Scope.default
                 )
        } yield assertTrue(
          e.isEmpty
        )
      },
      test("config with existing data") {
        val srcContent: String =
          writeJs(Map("this" -> "that", "abc" -> "123")).toString()
        val srcData: Source    = Source.WritableSource(srcContent)
        val tempWithData: Path = os.temp(contents = srcData)

        for {
          _this <-
            ZIO
              .serviceWithZIO[Config](_.get("this"))
              .provide(
                ConfigLive
                  .live(tempWithData.toIO.getParent(), tempWithData.last),
                Scope.default
              )
          abc   <- ZIO
                     .serviceWithZIO[Config](_.get("abc"))
                     .provide(
                       ConfigLive
                         .live(tempWithData.toIO.getParent(), tempWithData.last),
                       Scope.default
                     )
        } yield assertTrue(
          _this.contains("that"),
          abc.contains("123")
        )
      },
      test("config with no data") {
        val emptyData: Path = os.temp(contents = Source.WritableSource("{}"))

        for {
          e <- ZIO
                 .serviceWithZIO[Config](_.get("this"))
                 .provide(
                   ConfigLive
                     .live(emptyData.toIO.getParent(), emptyData.last),
                   Scope.default
                 )
        } yield assertTrue(
          e.isEmpty
        )
      },
      test("write data") {

        val emptyData: Path = os.temp(contents = Source.WritableSource("{}"))

        for {
          _       <- ZIO
                       .serviceWithZIO[Config](_.set("xyz", "omg"))
                       .provide(
                         ConfigLive
                           .live(emptyData.toIO.getParent(), emptyData.last),
                         Scope.default
                       )
          setData <- ZIO.attemptBlocking(os.read(emptyData))
        } yield assertTrue(
          read[Map[String, String]](setData).get("xyz").contains("omg")
        )
      },
      test("delete data") {

        val srcContent: String =
          writeJs(Map("this" -> "that")).toString()
        val srcData: Source    = Source.WritableSource(srcContent)
        val tempWithData: Path = os.temp(contents = srcData)

        for {
          _ <- ZIO
                 .serviceWithZIO[Config](_.delete("this"))
                 .provide(
                   ConfigLive
                     .live(tempWithData.toIO.getParent(), tempWithData.last),
                   Scope.default
                 )
        } yield assertTrue(
          os.read(tempWithData) == "{}"
        )
      }
    )
}
