package de.knutwalker.wdcm

import org.scalajs.dom
import org.scalajs.dom.raw.WebSocket
import org.scalajs.dom.{ ErrorEvent, Event, MessageEvent }

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.typedarray.TypedArrayBufferOps

import java.nio.ByteBuffer

trait Websockets {
  import TypedArrayBufferOps._

  private[this] var _unbind: () ⇒ Unit             = () ⇒ ()
  private[this] var ws     : js.UndefOr[WebSocket] = js.undefined
  private[this] val queue                          = mutable.Queue.empty[ByteBuffer]

  final def getWebsocketUri: String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host }/ws"
  }

  final def unbind() = {
    _unbind()
    _unbind = () ⇒ ()
  }

  final def open(f: (WebSocket, MessageEvent) ⇒ Unit) = {
    val ws = new WebSocket(getWebsocketUri)
    ws.binaryType = "arraybuffer"
    ws.onopen = { (e: Event) ⇒ aroundOnOpen(ws, e) }
    ws.onerror = { (e: ErrorEvent) ⇒ aroundOnError(ws, e) }
    ws.onclose = { (e: Event) ⇒ aroundOnClose(ws, e) }
    ws.onmessage = { (e: MessageEvent) ⇒ f(ws, e) }
    ws
  }

  final def send(bb: ByteBuffer) = {
    ws.filter(_.readyState == WebSocket.OPEN).fold {
      println(s"Got a message to send, but Websocket is not ready yet, buffering the message instead.")
      queue.enqueue(bb)
    }(_.send(bb.arrayBuffer()))
  }

  protected final def registerUnbind(f: js.Function0[Unit]): Unit = {
    val before = _unbind
    _unbind = () ⇒ {before(); f() }
  }

  protected final def aroundOnError(ws: WebSocket, event: ErrorEvent): Unit = {
    onError(ws, event)
    this.ws = js.undefined
    unbind()
  }

  protected final def aroundOnClose(ws: WebSocket, event: Event): Unit = {
    onClose(ws, event)
    this.ws = js.undefined
    unbind()
  }

  protected final def aroundOnOpen(ws: WebSocket, event: Event): Unit = {
    this.ws = ws
    onOpen(ws, event)
    if (queue.nonEmpty) {
      println(s"Sending ${queue.size} messages that had arrived before websocket was open.")
      while (queue.nonEmpty) {
        send(queue.dequeue())
      }
    }
  }

  protected def onError(ws: WebSocket, event: ErrorEvent): Unit = ()

  protected def onClose(ws: WebSocket, event: Event): Unit = ()

  protected def onOpen(ws: WebSocket, event: Event): Unit = ()
}
