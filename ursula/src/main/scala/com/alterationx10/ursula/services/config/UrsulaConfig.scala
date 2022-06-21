package com.alterationx10.ursula.services.config

import zio.*
import zio.json.*
import zio.stream.*
import java.util.UUID

trait UrsulaConfig {
  def get(key: String)(using namespace: String = ""): Task[Option[String]]
  def set(key: String, value: String)(using namespace: String = ""): Task[Unit]
  def delete(key: String)(using namespace: String = ""): Task[Unit]
}

object UrsulaConfig {
  def get(key: String)(using
      namespace: String = ""
  ): ZIO[UrsulaConfig, Throwable, Option[String]] =
    ZIO.serviceWithZIO[UrsulaConfig](_.get(key)(using namespace))

  def set(key: String, value: String)(using
      namespace: String = ""
  ): ZIO[UrsulaConfig, Throwable, Unit] =
    ZIO.serviceWithZIO[UrsulaConfig](_.set(key, value)(using namespace))

  def delete(key: String)(using
      namespace: String = ""
  ): ZIO[UrsulaConfig, Throwable, Unit] =
    ZIO.serviceWithZIO[UrsulaConfig](_.delete(key)(using namespace))
}

case class UrsulaConfigLive(
    configMap: Ref[Map[String, String]],
    dirty: Ref[Boolean]
) extends UrsulaConfig {

  private final val buildKey: String => String => String =
    key =>
      namespace => if (namespace == "") then key else s"${namespace}.${key}"

  def delete(key: String)(using namespace: String): Task[Unit] =
    for {
      _ <- configMap.getAndUpdate(_.removed(buildKey(key)(namespace)))
      _ <- dirty.set(true)
    } yield ()

  def get(key: String)(using namespace: String): Task[Option[String]] =
    configMap.get.map(_.get(key))

  def set(key: String, value: String)(using namespace: String): Task[Unit] =
    for {
      _ <- configMap.getAndUpdate(_ + (buildKey(key)(namespace) -> value))
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
