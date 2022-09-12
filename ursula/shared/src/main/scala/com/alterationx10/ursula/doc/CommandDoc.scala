package com.alterationx10.ursula.doc

import com.alterationx10.ursula.extensions.*
import com.alterationx10.ursula.command.Command

import scala.collection.mutable

final case class CommandDoc(cmd: Command) extends Documentation {

  override lazy val txt: String = {
    val sb: mutable.StringBuilder = new mutable.StringBuilder()
    sb.appendLine(s"${cmd.trigger}\t${cmd.description}")
    if (cmd.flags.nonEmpty) {
      sb.appendLine("Flags:")
      cmd.flags.sortBy(_.name).foreach { f =>
        sb.appendLine(f.documentation.txt.indented)
      }
    }
    if (cmd.arguments.nonEmpty) {
      sb.appendLine("Arguments:")
      cmd.arguments.foreach(a => sb.appendLine(a.documentation.txt.indented))
    }
    sb.appendLine("Usage:")
    sb.appendLine(s"\t${cmd.usage}")
    if (cmd.examples.nonEmpty) {
      sb.appendLine("Examples:")
      cmd.examples.foreach(e => sb.appendLine(s"\t$e"))
    }
    sb.toString()
  }

}
