package com.alterationx10.ursula.services

import upickle.default.*
import zio.*

import java.io.{IOException, PrintWriter}
import scala.io.{BufferedSource, Source}

trait Config {
  def get(key: String): Task[Option[String]]
  def set(key: String, value: String): Task[Unit]
  def delete(key: String): Task[Unit]
}

object Config {
  def get(key: String): ZIO[Config, Throwable, Option[String]] =
    ZIO.serviceWithZIO[Config](_.get(key))

  def set(key: String, value: String): ZIO[Config, Throwable, Unit] =
    ZIO.serviceWithZIO[Config](_.set(key, value))

  def delete(key: String): ZIO[Config, Throwable, Unit] =
    ZIO.serviceWithZIO[Config](_.delete(key))
}

case class ConfigLive(
    configMap: Ref[Map[String, String]],
    dirty: Ref[Boolean]
) extends Config {

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

object ConfigLive {

  private def readConfig(
      dir: String,
      file: String
  ): ZIO[Scope, Throwable, Source] =
    ZIO.fromAutoCloseable(ZIO.attempt(Source.fromFile(s"$dir/$file")))

  private def parseConfig(source: Source): Task[Map[String, String]] =
    ZIO.attempt {
      read[Map[String, String]](source.mkString)
    }

  private def printWriter(dir: String, file: String) =
    ZIO.fromAutoCloseable(ZIO.attempt(new PrintWriter(s"$dir/$file")))
  private def writeConfig(
      dir: String,
      file: String,
      config: ConfigLive
  ): ZIO[Scope, Nothing, Unit] = {
    for {
      writer <- printWriter(dir, file)
      cfg    <- config.configMap.get
      dirty  <- config.dirty.get
      _      <- ZIO.attempt(writeTo(cfg, writer)).when(dirty)
    } yield ()
  }.orDie

  def live(dir: String, file: String): ZLayer[Scope, Throwable, Config] =
    ZLayer {
      (for {
        cfg    <-
          readConfig(dir, file).orElse(ZIO.succeed(Source.fromString("{}")))
        map    <- parseConfig(cfg)
        mapRef <- Ref.make(map)
        dRef   <- Ref.make(false)
      } yield ConfigLive(mapRef, dRef)).withFinalizer { cfg =>
        writeConfig(dir, file, cfg)
      }
    }

}
