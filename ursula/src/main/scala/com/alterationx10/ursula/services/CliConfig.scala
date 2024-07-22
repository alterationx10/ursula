package com.alterationx10.ursula.services

import os.{Path, Source}
import upickle.default.*
import zio.*

trait CliConfig {
  def get(key: String): Task[Option[String]]
  def set(key: String, value: String): Task[Unit]
  def delete(key: String): Task[Unit]
}

object CliConfig {
  def get(key: String): ZIO[CliConfig, Throwable, Option[String]] =
    ZIO.serviceWithZIO[CliConfig](_.get(key))

  def set(key: String, value: String): ZIO[CliConfig, Throwable, Unit] =
    ZIO.serviceWithZIO[CliConfig](_.set(key, value))

  def delete(key: String): ZIO[CliConfig, Throwable, Unit] =
    ZIO.serviceWithZIO[CliConfig](_.delete(key))
}

case class CliConfigLive(
    configMap: Ref[Map[String, String]],
    dirty: Ref[Boolean]
) extends CliConfig {

  def delete(key: String): Task[Unit] =
    for {
      _ <- configMap.getAndUpdate(_.removed(key))
      _ <- dirty.set(true)
    } yield ()

  def get(key: String): Task[Option[String]] =
    configMap.get.map(_.get(key))

  def set(key: String, value: String): Task[Unit] =
    for {
      _ <- configMap.getAndUpdate(_ + (key -> value))
      _ <- dirty.set(true)
    } yield ()

}

object CliConfigLive {

  private def dirToPath(dir: String) =
    dir.split("/").filterNot(_.isEmpty).foldLeft(os.root)(_ / _)

  private def readConfig(
      dir: String,
      file: String
  ): ZIO[Any, Throwable, Map[String, String]] = {
    val configPath     = dirToPath(dir)
    val configFilePath = configPath / file
    for {
      _    <- ZIO.attempt(os.makeDir.all(configPath)).when(!os.exists(configPath))
      _    <- ZIO
                .attempt(os.write.over(configFilePath, "{}"))
                .when(!os.exists(configFilePath))
      data <- ZIO.attempt(os.read(configFilePath))
      cfg  <- ZIO.attempt {
                read[Map[String, String]](data)
              }
    } yield cfg
  }

  private def writeConfig(
      dir: String,
      file: String,
      config: CliConfigLive
  ): ZIO[Scope, Nothing, Unit] = {
    val configPath     = dirToPath(dir)
    val configFilePath = configPath / file
    for {
      cfg   <- config.configMap.get.map(d => writeJs(d).render(2))
      dirty <- config.dirty.get
      _     <- ZIO.attempt(os.write.over(configFilePath, cfg)).when(dirty)
    } yield ()
  }.orDie

  def live(dir: String, file: String): ZLayer[Scope, Throwable, CliConfig] =
    ZLayer {
      (for {
        cfg    <-
          readConfig(dir, file)
        mapRef <- Ref.make(cfg)
        dRef   <- Ref.make(false)
      } yield CliConfigLive(mapRef, dRef))
        .withFinalizer { cfg =>
          writeConfig(dir, file, cfg)
        }
    }

  def temp: ZLayer[Scope, Throwable, CliConfig] = {

    val tmpDir: Path =
      os.temp.dir(prefix = "ursula-config-temp")

    val tempFile: Path =
      os.temp(contents = Source.WritableSource("{}"), dir = tmpDir)

    live(tmpDir.toString, tempFile.last)
  }

}
