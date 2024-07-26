package com.alterationx10.ursula.errors

sealed trait FlagException extends UrsulaException

case object InvalidOptionFlagException extends FlagException {
  val msg: String =
    "The value you've given is not a valid option for this flag."
}
