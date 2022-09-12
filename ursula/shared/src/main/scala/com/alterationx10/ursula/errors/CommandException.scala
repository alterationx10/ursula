package com.alterationx10.ursula.errors

sealed trait CommandException extends UrsulaException

case object HelpFlagException extends CommandException {
  val msg: String = ""
}

case object UnrecognizedFlagException extends CommandException {
  val msg: String = "You have given an unrecognized flag."
}

case object MissingFlagsException extends CommandException {
  val msg: String = "Missing a required flag."
}

case object ConflictingFlagsException extends CommandException {
  val msg: String = "You have used conflicting flags."
}
