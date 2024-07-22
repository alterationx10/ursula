package com.alterationx10.ursula.services

import os.{Path, Source}
import upickle.default.*
import zio.{Runtime, Scope, ZIO, ZLayer}
import zio.test.*

object CliConfigSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ConfigSpec")(
      test("config doesn't exist") {
        val tempNoExist: Path = os.temp()
        os.remove(tempNoExist)
        for {
          e <- ZIO
                 .serviceWithZIO[CliConfig](_.get(""))
                 .provide(
                   CliConfigLive
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
              .serviceWithZIO[CliConfig](_.get("this"))
              .provide(
                CliConfigLive
                  .live(tempWithData.toIO.getParent(), tempWithData.last),
                Scope.default
              )
          abc   <- ZIO
                     .serviceWithZIO[CliConfig](_.get("abc"))
                     .provide(
                       CliConfigLive
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
                 .serviceWithZIO[CliConfig](_.get("this"))
                 .provide(
                   CliConfigLive
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
                       .serviceWithZIO[CliConfig](_.set("xyz", "omg"))
                       .provide(
                         CliConfigLive
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
                 .serviceWithZIO[CliConfig](_.delete("this"))
                 .provide(
                   CliConfigLive
                     .live(tempWithData.toIO.getParent(), tempWithData.last),
                   Scope.default
                 )
        } yield assertTrue(
          os.read(tempWithData) == "{}"
        )
      }
    )
}
