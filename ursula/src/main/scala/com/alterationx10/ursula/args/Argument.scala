package com.alterationx10.ursula.args

import zio._

trait Argument {
  def describe: String          = ???
  def describeZIO: Task[String] = ???
}
