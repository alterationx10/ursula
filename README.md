# Ursula

A slim framework to make CLI apps in ZIO.

## Project Status

This project is still in (very) early development, but feel free to give it a
try - I'd love to hear any feedback you have. To add resolvers for the published
jars, check the [Using in your project](#using-in-your-project) section.

I'm currently publishing `0.0.0-a# ` builds, any they might (i.e. most likely
will) break a bit on updates, but using the current latest will give you a feel
for how it's used, and where the project is moving towards.

## Scala 2 Compatibility

This project is written in Scala 3, but without too many fancy new features. I
am trying to support cross-compile and publish for Scala 2.13 as well (starting
at 0.0.0-a6).

This means it should be straightforward to use in either case, without needing
the `-Ytasty-reader` compiler flag or `CrossVersion` updates on the dependency:

```scala
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / resolvers += "alterationx10-ursula" at "https://dl.cloudsmith.io/public/alterationx10/ursula/maven/"

lazy val scala3Project = (project in file("proj-scala3"))
  .settings(
    name := "urusla-app_2",
    scalaVersion := "3.1.2",
    libraryDependencies += "com.alterationx10" %% "ursula" % "0.0.0-a6"
  )

lazy val scala2Project = (project in file("proj-scala2"))
  .settings(
    name := "urusla-app_3",
    scalaVersion := "2.13.8",
    // scalacOptions += "-Ytasty-reader", <- not needed
    libraryDependencies += ("com.alterationx10" %% "ursula" % "0.0.0-a6")//.cross(CrossVersion.for2_13Use3) <- not needed
  )
```

## About

One of my personal motivations for this library, is that I want to take useful
code I already have, and make a CLI out of it. Often times I have a collection
of scripts/scratch projects that could easily be parameterized, and be shared
with my _non-Scala_ friends.

A lot of this framework tries to bootstrap that idea - how can we turn this type
of code into helpful, shareable apps that take care of the boiler plate of
bootstrapping, and provide documentation needed to let others know how to run
it.

## Anatomy of the Framework

Here is a general overview if of the pieces fit together.

### How it works: UrsulaApp

You only need to make an object that extends the `UrsulaApp` trait, and provide
a `Seq[Command[?]]`, which are your actions you wish to be available in your
app. `UrsulaApp` extends `ZIOAppDefault`, and wires up everything needed for the
`run` method automatically! It parses the arguments passed, and uses that to
pull out the `Command` provided, and runs accordingly, passing on the arguments
to it.

There are some [built in commands](#built-in-commands) provided, such as the
`HelpCommand` and `ConfigCommand`, that are also automatically injected. This
means that even if you only have:

```scala
object App extends UrsulaApp {
    override val commands: Seq[Command[?]] = Seq.empty
}
```

you already have a functioning cli app that has a `help` command that prints all
the available commands accepted (as little as they are so far), as well as a
`config` command that will let a user set/get/delete values from the apps config
file (defaulted to `$home/.ursula/config.json`)

At this point, you need only implement some `Command`s that wrap the
functionality you desire, and add them to the `commands: Seq`.

### Commands

There is a `trait Command[A]` to extend, and the essence of this the
implementation of

```scala
def action(args: Chunk[String]): ZIO[UrsulaServices, Throwable, A]
```

You consolidate all of your ZIO logic you want to run in this method that will
pass in arguments that are `Chunk[String]` and returns a
`ZIO[UrsulaServices, Throwable, A]`.

`UrsulaServices` are a collection of built in ZIO services that are available
for you to use (or not at all, if you don't want to!). `UrsulaServices` are
bundled in via the `UrsulaApp`, so you don't need to provide them with your
logic - you just need to provide all of the other resources to run your won
logic (i.e. you can `.provideSome[UrsulaServices](...yourDepsHere))` and not
have to worry about it.

There are a few other items to implement, such as

```scala
  val trigger: String
  val description: String
  val usage: String
  val examples: Seq[String]
```

`trigger` is the String that should be used at the start of your CLI arguments
to call that particular command. The others are simple informational strings
about your command - and those are automatically used by the built-in help
command to print documentation!

Two other important things to declare are

```scala
  val flags: Seq[Flag[?]]
  val arguments: Seq[Argument[?]]
```

[Flags](#flags) and [Arguments](#arguments) are discussed below, but know that
they are simple traits to extend that help you parse/provide values for the
`args` passed in - and they too have some simple Strings to implement that
provide auto documentation for your app. At the end of the day, you can just
parse the `args` on your own in your ZIO logic - but usage of the `Flag`s
and`Arguments` should hopefully simplify things for your and your apps users.

#### Built-In Commands

- HelpCommand - handles the printing of documentation
- ConfigCommand - manipulates a simple JSON config file

### Flags

Flags (`trait Flag[R]`) are non-positional arguments passed to the command.
Flags can be generally used as either an argument flag, which expects the next
element in the command arguments to be parsed as type `R`, or boolean flags
which do not (i.e. present/not present).

The
[source code](./ursula/src/main/scala/com/alterationx10/ursula/args/Flag.scala)
is fairly well documented at this point. Some general highlights are that it has
things built in to

- parse argument(s) that can then be used in you `Command`
- declare conflicts with other flags
- declare requirements of other flags
- provide defaults, of ENV variables to be used

### Arguments

Arguments (`trait Argument[R]`) are _positional_ arguments passed to the
command, and are to be parsed to type `R`

The
[source code](./ursula/src/main/scala/com/alterationx10/ursula/args/Argument.scala)
is fairly well documented at this point. Some general highlights are that you
can encode the parsing logic.

### Built-In Services

`UrsulaServices` is the sum of all services that are automatically provided in
the `R` channel of your commands `action` zio. See below for more info on each.

#### UrsulaConfig

This service allows you to get/set/delete keys from the apps config file. Behind
the scenes, the `UrsulaApp` will automatically read the config file at start,
and load it into an in memory `Map`. This `Map` is what backs this service, and
if the state becomes dirty (i.e. a `set` or `delete` is used), then after your
`Command` logic has run, it will persist the `Map` back to disk.

## Using in your project

[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=flat-square)](https://cloudsmith.com)

Add this resolver to your build.sbt file:

resolvers += "alterationx10-ursula" at
"https://dl.cloudsmith.io/public/alterationx10/ursula/maven/"

## Example

This repo contains a very simple
[example program](./example/src/main/scala/com/alterationx10/example/), which
contains an `echo` command and a couple simple flags. Drop into an sbt repl, and
run `example/run` to give it a try!

## Other Projects

It's always good to have options 😉.

There is an official [zio-cli](https://github.com/zio/zio-cli) project out
there, and I encourage you to check that out too! I feel maybe one large
difference is just how apps are structured/built, driven mostly by my
motivations outlined [above](#about). A lot of my inspiration for this project
comes from [oclif: Node.JS Open CLI Framework ](https://github.com/oclif/oclif).
