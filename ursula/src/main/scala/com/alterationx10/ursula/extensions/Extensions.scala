package com.alterationx10.ursula.extensions

import zio.Chunk

extension (s: String) {
  def chunked: Chunk[String] = Chunk.fromArray(s.split(" "))
  def indented: String       =
    s.split(System.lineSeparator())
      .map(s => "\t" + s)
      .mkString(System.lineSeparator())
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

extension (sb: StringBuilder) {
  def newLine: StringBuilder =
    sb.append(System.lineSeparator())

  def appendLine(str: String): StringBuilder =
    sb.append(str + System.lineSeparator())
}
