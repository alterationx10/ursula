package com.alterationx10.example

import com.alterationx10.example.commands.Echo
import com.alterationx10.ursula.UrsulaApp
import com.alterationx10.ursula.command.Command

import zio.ZLayer

object Example extends UrsulaApp {

  override val commands: Seq[Command[?]] = Seq(Echo())

}
