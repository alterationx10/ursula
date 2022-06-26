Ursula
======

## Using in your project

[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=flat-square)](https://cloudsmith.com)

Add this resolver to your build.sbt file:

resolvers += "alterationx10-ursula" at "https://dl.cloudsmith.io/public/alterationx10/ursula/maven/"

## About

*still early days*

A slim framework to make CLI apps in ZIO, inspired by [oclif](https://github.com/oclif/oclif).

The idea is that you can compartmentalize some ZIO logic via extending `Command[A]`. The `A` type is the
return type, allowing you to depend/chain commands in other `Command[_]`s, so you can repurpose logic.

There is a `trait UrsulaApp extends ZIOAppDefault` which hooks up all the plumbing, needing only to implement
`val commandLayer: ZLayer[Any, Nothing, Seq[Command[_]]]`.

You can see the quick and dirty output of the example via one of:

```
sbt "example/run echo \"this is helpful\""
sbt "example/run echo -l \"this is helpful\""
sbt "example/run echo -s \"this is helpful\""
sbt "example/run echo --help"
```
