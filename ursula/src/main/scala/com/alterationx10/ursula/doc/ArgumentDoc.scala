package com.alterationx10.ursula.doc

import com.alterationx10.ursula.args.Argument
import com.alterationx10.ursula.doc.*

final case class ArgumentDoc(arg: Argument[?]) extends Documentation {
  override lazy val txt: String = {
    val sb = new StringBuilder()
    sb.append(s"${arg.name}\t${arg.description}")
    if (arg.required) {
      sb.append(" [required]")
    }
    sb.toString()
  }
}
