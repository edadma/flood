package io.github.edadma.flood

import cps.*
import cps.monads.FutureAsyncMonad
import io.github.spritzsn.async.*

import java.net.URL
import scala.concurrent.Future
import scala.util.{Failure, Success}

@main def run(): Unit = async {
  var cont = true
  val url = "localhost:3000"
  var count = 0

  def request = fetch(url) andThen (_ => count += 1)

  def user = async {
    while cont do await(request).text()
  }

  user
  user
  user
  user
  user
  await(timer(1000))
  cont = false
  println(count)
}
