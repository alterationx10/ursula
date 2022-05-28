package com.alterationx10.ursula.args

import zio._

import scala.annotation.tailrec

/** Flags are non-positional arguments passed to the command. Flags can be
  * generally used as either an argument flag, which expects an argument parsed
  * as type R, or boolean flags which do not.
  * @tparam R
  *   The type expected to parse the flag argument as.
  */
trait Flag[R] {

  /** The name of the flag, e.g. "help". This will be parsed as s"--$name", e.g.
    * "--help"
    */
  val name: String

  /** A short-key version of name, e.g. "h". This will be parsed as
    * s"-$shortKey", e.g. "-h"
    */
  val shortKey: String

  /** A description of the purpose of this flag, used when printing help.
    */
  val description: String

  /** A flag to set whether this Flag should be printed in help.
    */
  val hidden: Boolean = false

  /** A flag to indicate if this Flag can be used multiple times in one Command
    */
  val multiple: Boolean = false

  /** An optional environment variable used to set the value of the this Flag.
    * Arguments passed in take precedence over environment settings.
    */
  val env: Option[String] = Option.empty

  /** An optional set of possible values to restrict the argument to. For
    * example, if you wanted to restrict an --env [arg] flag to only "dev", or
    * "test", supply them here.
    */
  val options: Option[Set[String]] = Option.empty

  /** A partial function that will take the String value of the passed argument,
    * and convert it to type R.
    *
    * Typical usage might be for parsing String to Int/Float/Custom Domain, e.g.
    * for a Flag[Int], parse = str => str.toInt.
    *
    * Advanced usage could be used to do more targeted work as well, such as
    * processing the argument directly, versus simply obtaining the value for it
    * to be parsed later, e.g. for a Flag[String], parse = str =>
    * str.toUpperCase
    * @return
    *   the evaluation of String => R
    */
  def parse: PartialFunction[String, R]

  /** A partial function that will take the String value of the passed argument,
    * and convert it to a Task[R]. See [[parse]].
    *
    * By default, it wraps [[parse]] in a ZIO.attempt
    *
    * @return
    *   The evaluation of String => Task[R]
    */
  def parseZIO: PartialFunction[String, Task[R]] =
    str => ZIO.attempt(parse(str))

  /** An optional default value to provide
    */
  val default: Option[R] = Option.empty

  /** A flag to indicate if this Flag is required or not.
    */
  val required: Boolean = false

  /** An optional set of Flags that need to also be present for this Flag to
    * function
    */
  val dependsOn: Option[Seq[Flag[_]]] = Option.empty

  /** An optional set of Flags that conflict with usage of this Flag
    */
  val exclusive: Option[Seq[Flag[_]]] = Option.empty

  final lazy val _sk: String = s"-$shortKey"
  final lazy val _lk: String = s"--$name"

  /** Checks if flags to trigger this Flag are present in the provided Command
    * arguments
    * @param args
    *   The arguments passed to the command
    * @return
    *   true if present, otherwise false
    */
  final def isPresent(args: Chunk[String]): Boolean =
    args.find(a => a == _sk || a == _lk).isDefined

  /** Wraps [[isPresent]] in a ZIO.attempt
    * @param args
    *   The arguments passed to the command
    * @return
    *   ZIO.attempt(isPresent(args))
    * @see
    *   [[isPresent]]
    */
  final def isPresentZIO(args: Chunk[String]): Task[Boolean] =
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

  /** A method that will find the first instance of a flag triggering this Flag,
    * if present, and evaluate the [[parse]] partial function on it.
    *
    * Most useful when this Flag is expected once (i.e. [[multiple]] == false)
    * @param args
    *   The arguments passed to the command
    * @return
    * @see
    *   [[multiple]]
    */
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
