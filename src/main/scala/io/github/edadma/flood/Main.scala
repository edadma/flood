package io.github.edadma.flood

import cps.*
import cps.monads.FutureAsyncMonad

import io.github.spritzsn.async._

@main def run(): Unit = async {
  println(await(fetch("localhost")).text())
}
