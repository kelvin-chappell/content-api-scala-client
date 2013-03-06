package com.gu.openplatform.contentapi.util

import com.gu.openplatform.contentapi.ApiError

/** Monad typeclass with failure specialized to ApiError
  */
trait Monad[F[_]] {

  def point[A](a: A): F[A]

  def bind[A, B](f: A => F[B]): F[A] => F[B]

  /** `fail` is not really part of the monad contract and can not be implemented for
    * all monads without throwing an exception (as in the case of the Identity monad).
    * We replicate this historical piece of pragmatism (from Haskell) here, because
    * throwing the exception is the intended behaviour for the blocking client.
    */
  def fail[A](error: ApiError): F[A]
}

/** Provides monadic operations as method syntax. This enables us to use
  * for-comprehensions over the abstract monad type in the Api implementation.
  */
final class MonadOps[M[_], A](ma: M[A])(implicit M: Monad[M]) {
  def map[B](f: A => B): M[B] = M.bind(f andThen M.point)(ma)
  def flatMap[B](f: A => M[B]): M[B] = M.bind(f)(ma)
}

/** Provides an implicit conversion from monadic types to MonadOps
  */
object MonadOps {

  implicit def monadOps[M[_]:Monad, A](ma: M[A]): MonadOps[M, A] = new MonadOps(ma)

  def point[M[_], A](a: A)(implicit M: Monad[M]): M[A] = M.point(a)
  def fail[M[_], A](error: ApiError)(implicit M: Monad[M]): M[A] = M.fail(error)
}
