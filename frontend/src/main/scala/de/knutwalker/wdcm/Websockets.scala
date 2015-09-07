package de.knutwalker.wdcm

import org.scalajs.dom
import org.scalajs.dom.raw.WebSocket
import org.scalajs.dom.{ ErrorEvent, Event, MessageEvent }

import scala.scalajs.js
import scala.scalajs.js.typedarray.TypedArrayBufferOps

import java.nio.ByteBuffer

trait Websockets {
  import TypedArrayBufferOps._

  private[this] var _unbind: () ⇒ Unit             = () ⇒ ()
  private[this] var ws     : js.UndefOr[WebSocket] = js.undefined

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

    ws.filter(_.readyState == WebSocket.OPEN).foreach(_.send(bb.arrayBuffer()))

  final def send(bb: ByteBuffer) = {

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
  }

  protected def onError(ws: WebSocket, event: ErrorEvent): Unit = ()

  protected def onClose(ws: WebSocket, event: Event): Unit = ()

  protected def onOpen(ws: WebSocket, event: Event): Unit = ()
}
