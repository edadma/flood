package io.github.edadma.flood

import scala.collection.immutable
import scala.concurrent.{Future, Promise}
import cps.*
import cps.monads.FutureAsyncMonad
import io.github.spritzsn.async.*
import io.github.spritzsn.libuv.{AddrInfo, Buffer, TCP, checkError, defaultLoop, eof, errName, strError}

import scala.io.Codec
import scala.scalanative.posix.sys.socket.AF_INET
import scala.util.{Failure, Success}

case class FetchResult(
    version: String,
    status: Int,
    reason: String,
    headers: Map[String, String],
    body: immutable.ArraySeq[Byte],
):
  def text(codec: Codec = Codec.UTF8): String = new String(body.toArray, codec.charSet)

private val URLRegex = """(?:(.+)://)?([^:/]+)?(?::([0-9]+))?(/.*)?""".r

def fetch(url: String): Future[FetchResult] =
  url match
    case URLRegex(scheme, domain, port, path) =>
      getAddrInfo(domain, null, AF_INET) flatMap { (status, addrInfo) =>
        checkError(status, "getAddrInfo")
        fetch(addrInfo.head.ip, if port == null then 80 else port.toInt, domain, path)
      }
    case _ => sys.error("error parsing error")

def fetch(ip: String, port: Int, domain: String, path: String): Future[FetchResult] =
  val promise = Promise[FetchResult]()
  val parser = new HTTPResponseParser
  val h = defaultLoop.tcp

  def connectCallback(status: Int): Unit =
    h.write(s"GET ${if path == null then "/" else path} HTTP/1.0\r\nHost: $domain\r\n\r\n".getBytes)

    def readCallback(stream: TCP, size: Int, buf: Buffer): Unit =
      if size < 0 then
        stream.readStop
        if size != eof then
          promise.failure(new RuntimeException(s"error in read callback: ${errName(size)}: ${strError(size)}"))
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
  end connectCallback

  h.connect(ip, port, connectCallback)
  promise.future
