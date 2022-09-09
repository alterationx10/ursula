package com.alterationx10.ursula.extensions

import zio.*

trait ZIOTestExtensions {

  implicit class ZLayerTestExtension[E, A](layer: ZLayer[Any, E, A]) {

    def testRuntime: Runtime.Scoped[A] = {
      Unsafe.unsafe { implicit unsafe =>
        Runtime.unsafe.fromLayer(layer)
      }
    }

  }

  implicit class ZIOUTestExtension[R, E, A](zio: ZIO[R, E, A]) {
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
