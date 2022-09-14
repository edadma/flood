package io.github.edadma.flood

import cps.*
import cps.monads.FutureAsyncMonad
import io.github.spritzsn.async.*

import java.net.URL
import scala.concurrent.Future
import scala.util.{Failure, Success}

@main def run(): Unit = async {
  println(await(fetch("localhost:3000")).text())
}
