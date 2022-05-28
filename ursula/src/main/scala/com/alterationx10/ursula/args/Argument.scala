package com.alterationx10.ursula.args

import zio._

/** Arguments are positional arguments passed to the command, and can be parsed
  * to type R
  */
trait Argument[R] {

  /** Name of the flag, to be printed with help
    */
  val name: String

  /** Description of the purpose of this Argument
    */
  val description: String

  /** Sets whether this is required or not
    */
  val required: Boolean = false

  /** Sets whether this Argument should be printed in help
    */
  val hidden: Boolean = false

  /** A partial function that will take the String value of the passed argument,
    * and convert it to type R.
    *
    * Typical usage might be for parsing String to Int/Float/Custom Domain, e.g.
    * for a Argument[Int], parse = str => str.toInt.
    *
    * Advanced usage could be used to do more targeted work as well, such as
    * processing the argument directly, versus simply obtaining the value for it
    * to be parsed later, e.g. for a Argument[String], parse = str =>
    * str.toUpperCase
    * @return
    *   the evaluation of String => R
    */
  def parse: PartialFunction[String, R]

  /** Wraps [[parse]] in ZIO.attempt
    */
  def parseZIO: PartialFunction[String, Task[R]] =
    str => ZIO.attempt(parse(str))

  /** An optional set of possible values to restrict the argument to. For
    * example, if you wanted to restrict to only "dev", or "test", supply them
    * here.
    */
  val options: Option[Set[Argument[_]]] = Option.empty

  /** An optional default value to apply
    */
  val default: Option[R] = Option.empty

  /** Print documentation to the console.
    */
  def describeZIO: Task[Unit] = for {
    _ <- Console.printLine(
           s"\t$name\t$description${if (required) " [required" else ""}"
         )
  } yield ()
}
