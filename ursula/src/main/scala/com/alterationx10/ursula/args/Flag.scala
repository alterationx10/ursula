package com.alterationx10.ursula.args

import zio._

import scala.annotation.tailrec

trait Flag[R] {
  val name: String
  val shortKey: String
  val description: String
  val hidden: Boolean                            = false
  val multiple: Boolean                          = false
  val env: Option[String]                        = Option.empty
  val options: Option[Set[String]]               = Option.empty
  def parse: PartialFunction[String, R]
  def parseZIO: PartialFunction[String, Task[R]] =
    str => ZIO.attempt(parse(str))
  val default: Option[R]                         = Option.empty
  val required: Boolean                          = false
  val dependsOn: Option[Seq[Flag[_]]]            = Option.empty
  val exclusive: Option[Seq[Flag[_]]]            = Option.empty

  final lazy val _sk: String = s"-$shortKey"
  final lazy val _lk: String = s"--$name"

  def isPresent(args: Chunk[String]): Boolean =
    args.find(a => a == _sk || a == _lk).isDefined

  def isPresentZIO(args: Chunk[String]): Task[Boolean] =
    ZIO.attempt(isPresent(args))

  private final def recursiveParse[A](
      fn: String => A
  )(args: Chunk[String]): Chunk[A] = {
    @tailrec
    def loop(a: Chunk[String], r: Chunk[A]): Chunk[A] =
      a.toList match {
        case Nil                                   => r
        case _ :: Nil                              => r
        case f :: v :: _ if (f == _sk || f == _lk) =>
          loop(a.drop(2), r.appended(fn(v)))
        case _                                     =>
          loop(a.drop(1), r)
      }
    loop(args, Chunk.empty)
  }

  def parseFirstArg(args: Chunk[String]): Option[R] =
    args.dropUntil(a => a == _sk || a == _lk).headOption.map(parse)

  def parseFirstArgZIO(args: Chunk[String]): Task[Option[R]] =
    ZIO.attempt(parseFirstArg(args))

  def parseArgs(args: Chunk[String]): Chunk[R] = recursiveParse(parse)(args)

  def parseArgsZIO(args: Chunk[String]): Chunk[Task[R]] =
    recursiveParse(parseZIO)(args)

  def describe: String          = ???
  def describeZIO: Task[String] = ???
}

trait BooleanFlag extends Flag[Boolean] {
  override def parse: PartialFunction[String, Boolean] = _ => false
}

trait StringFlag extends Flag[String] {
  override def parse: PartialFunction[String, String] = identity[String](_)
}
