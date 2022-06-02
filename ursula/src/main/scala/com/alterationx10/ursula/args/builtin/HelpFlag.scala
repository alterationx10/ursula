package com.alterationx10.ursula.args.builtin

import com.alterationx10.ursula.args.BooleanFlag

case object HelpFlag extends BooleanFlag {
  override val name: String        = "help"
  override val shortKey: String    = "h"
  override val description: String = "Prints help"
}
