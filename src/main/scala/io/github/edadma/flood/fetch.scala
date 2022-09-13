package io.github.edadma.flood

import scala.collection.immutable
import scala.concurrent.{Future, Promise}
import cps.*
import cps.monads.FutureAsyncMonad
import io.github.spritzsn.libuv.{AddrInfo, Buffer, TCP, defaultLoop, eof, errName, strError}

import scala.scalanative.posix.sys.socket.AF_INET

case class FetchResult(
    version: String,
    status: Int,
    reason: String,
    headers: Map[String, String],
    body: immutable.ArraySeq[Byte],
)

def fetch(url: String): Future[FetchResult] =
  val promise = Promise[FetchResult]()
  val parser = new HTTPResponseParser

  def dnsCallback(status: Int, addrInfo: List[AddrInfo]): Unit =
    println(s"dnsCallback status: $status; addrInfo $addrInfo")

    val h = defaultLoop.tcp

    def connectCallback(status: Int): Unit =
      println(s"connectCallback status: $status; error: ${strError(status)}")
      h.write("GET / HTTP/1.0\r\nHost: localhost\r\n\r\n".getBytes)

      def readCallback(stream: TCP, size: Int, buf: Buffer): Unit =
        println(s"readCallback size: $size eof $eof")
        if size < 0 then
          stream.readStop
          if size != eof then println(s"error in read callback: ${errName(size)}: ${strError(size)}") // todo
          else if size > 0 then
            try
              for i <- 0 until size do parser send buf(i)
              if parser.isFinal then
                promise.success(
                  FetchResult(
                    parser.version,
                    parser.status,
                    parser.reason,
                    parser.linkedHeaders.toMap,
                    parser.body to immutable.ArraySeq,
                  ),
                )
            catch case e: Exception => promise.failure(e)
      end readCallback

      h.readStart(readCallback)

    h.connect(addrInfo.head.ip, 3000, connectCallback)

  defaultLoop.getAddrInfo(dnsCallback, "localhost", null, AF_INET)
  promise.future
