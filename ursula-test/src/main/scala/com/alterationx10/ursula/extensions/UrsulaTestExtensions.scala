package com.alterationx10.ursula.extensions

import zio.*

trait UrsulaTestExtensions {

  implicit class ZLayerTestExtension[E, A](layer: ZLayer[Any, E, A]) {

    def testRuntime: Runtime.Scoped[A] = {
      Unsafe.unsafe { implicit unsafe =>
        Runtime.unsafe.fromLayer(layer)
      }
    }

  }

  implicit class ZLayerTestScopedExtension[E, A](layer: ZLayer[Scope, E, A]) {

    def testRuntime: Runtime.Scoped[A] = {
      Unsafe.unsafe { implicit unsafe =>
        Runtime.unsafe.fromLayer(Scope.default >>> layer)
      }
    }

  }

  implicit class ZIOTestExtension[R, E, A](zio: ZIO[R, E, A]) {
    def testValue(implicit runtime: Runtime[R]): A = {
      Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe
          .run(
            zio
          )
          .getOrThrowFiberFailure()
      }
    }

    def runTestEither(implicit runtime: Runtime[R]): Either[E, A] = {
      Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe
          .run(
            zio.either
          )
          .getOrThrowFiberFailure()
      }
    }

    def expect(exp: A => Boolean)(implicit runtime: Runtime[R]): Boolean = {
      exp(zio.testValue)
    }
  }

}
