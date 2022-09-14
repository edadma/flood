package io.github.edadma.flood

import cps.*
import cps.monads.FutureAsyncMonad
import io.github.spritzsn.async.*

import java.net.URL
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@main def run(users: Int, seconds: Double): Unit = async {
  var cont = true
  var count = 0

  def request = fetch("127.0.0.1", 3000, "localhost", "/") andThen (_ => count += 1)

  def user = async {
    while cont do await(request)
  }

  for _ <- 1 to users do user
  await(timer(seconds second))
  cont = false
  println(s"$count total; ${(count / seconds).toInt} transactions per second")
}
