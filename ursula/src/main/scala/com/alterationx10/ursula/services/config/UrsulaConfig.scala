package com.alterationx10.ursula.services.config

import zio.*
import zio.json.*
import zio.stream.*
import java.util.UUID

trait UrsulaConfig {
  def get(key: String): Task[Option[String]]
  def set(key: String, value: String): Task[Unit]
  def delete(key: String): Task[Unit]
}

object UrsulaConfig {
  def get(key: String): ZIO[UrsulaConfig, Throwable, Option[String]] =
    ZIO.serviceWithZIO[UrsulaConfig](_.get(key))

  def set(key: String, value: String): ZIO[UrsulaConfig, Throwable, Unit] =
    ZIO.serviceWithZIO[UrsulaConfig](_.set(key, value))

  def delete(key: String): ZIO[UrsulaConfig, Throwable, Unit] =
    ZIO.serviceWithZIO[UrsulaConfig](_.delete(key))
}

case class UrsulaConfigLive(
    configMap: Ref[Map[String, String]],
    dirty: Ref[Boolean]
) extends UrsulaConfig {

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

object UrsulaConfigLive {

  val live: ZLayer[Any, Nothing, UrsulaConfig] = ZLayer.fromZIO {
    for {
      map  <- Ref.make(Map.empty[String, String])
      bool <- Ref.make(false)
    } yield UrsulaConfigLive(map, bool)
  }

}
