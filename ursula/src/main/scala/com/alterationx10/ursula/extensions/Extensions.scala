package com.alterationx10.ursula.extensions

import zio.Chunk

extension (s: String) {
  def chunked: Chunk[String] = Chunk.fromArray(s.split(" "))
}

extension [A](c: Chunk[A]) {
  def :~(a: A): Chunk[A]         = if c.isEmpty then Chunk(a) else c
  def :~(a: Option[A]): Chunk[A] = if c.isEmpty then Chunk(a).flatten else c
  def :~(a: Chunk[A]): Chunk[A]  = if c.isEmpty then a else c
}

extension [A](o: Option[A]) {
  def :~(a: A): Option[A]         = o.orElse(Option(a))
  def :~(a: Option[A]): Option[A] = o.orElse(a)
}
