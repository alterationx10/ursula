package com.alterationx10.ursula.extensions

import com.alterationx10.ursula.errors.UrsulaException
import zio.Chunk

import scala.annotation.targetName

extension (s: String) {
  def chunked: Chunk[String] = Chunk.fromArray(s.split(" "))
  def indented: String       =
    s.split(System.lineSeparator())
      .map(s => "\t" + s)
      .mkString(System.lineSeparator())
}

extension [A](c: Chunk[A]) {
  @targetName("nonEmptyOrElseDefault")
  def :~(a: A): Chunk[A]         = if c.isEmpty then Chunk(a) else c
  @targetName("nonEmptyOrElseDefault")
  def :~(a: Option[A]): Chunk[A] = if c.isEmpty then Chunk(a).flatten else c
  @targetName("nonEmptyOrElseDefault")
  def :~(a: Chunk[A]): Chunk[A]  = if c.isEmpty then a else c

  def oneOfOrThrow[E <: UrsulaException](options: Set[A], err: E): Chunk[A] = {
    if !c.foldLeft(true)((b, e) =>
        b && (options.isEmpty || options.contains(e))
      )
    then throw err
    else c
  }
}

extension [A](o: Option[A]) {
  @targetName("nonEmptyOrElseDefault")
  def :~(a: A): Option[A]         = o.orElse(Option(a))
  @targetName("nonEmptyOrElseDefault")
  def :~(a: Option[A]): Option[A] = o.orElse(a)

  def oneOfOrThrow[E <: UrsulaException](options: Set[A], err: E) =
    if (o.isDefined && options.nonEmpty) {
      val filtered = o.filter(e => options.contains(e))
      if (filtered.isEmpty) throw err else filtered
    } else o
}

extension (sb: StringBuilder) {
  def newLine: StringBuilder =
    sb.append(System.lineSeparator())

  def appendLine(str: String): StringBuilder =
    sb.append(str + System.lineSeparator())
}
