package com.alterationx10.ursula

import zio.Chunk

package object extensions {

  implicit class StringExtension(s: String) {
    def chunked: Chunk[String] = Chunk.fromArray(s.split(" "))
    def indented: String       =
      s.split(System.lineSeparator())
        .map(s => "\t" + s)
        .mkString(System.lineSeparator())
  }

  implicit class ChunkExtension[A](c: Chunk[A]) {
    def :~(a: A): Chunk[A]         = if (c.isEmpty) Chunk(a) else c
    def :~(a: Option[A]): Chunk[A] = if (c.isEmpty) Chunk(a).flatten else c
    def :~(a: Chunk[A]): Chunk[A]  = if (c.isEmpty) a else c
  }

  implicit class OptionExtension[A](o: Option[A]) {
    def :~(a: A): Option[A]         = o.orElse(Option(a))
    def :~(a: Option[A]): Option[A] = o.orElse(a)
  }

  implicit class StringBuilderExtensions(sb: StringBuilder) {
    def newLine: StringBuilder =
      sb.append(System.lineSeparator())

    def appendLine(str: String): StringBuilder =
      sb.append(str + System.lineSeparator())
  }

}
