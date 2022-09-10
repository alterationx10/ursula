package com.alterationx10.ursula.services

import zio.*

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

  val live: ZLayer[Any, Nothing, Config] = ZLayer.fromZIO {
    for {
      map  <- Ref.make(Map.empty[String, String])
      bool <- Ref.make(false)
    } yield ConfigLive(map, bool)
  }

}
