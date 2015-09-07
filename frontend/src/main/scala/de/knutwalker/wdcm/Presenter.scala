package de.knutwalker.wdcm

import de.knutwalker.bespoke.{ BespokeEvent, Deck, Plugin }
import org.scalajs.dom.Event
import org.scalajs.dom.raw.WebSocket
import boopickle.Default.Pickle

case object Presenter extends Plugin with Websockets {

  override protected def onClose(ws: WebSocket, event: Event): Unit =
    sendEvent(BespokeMessage.disconnect)

  override def apply(deck: Deck): Unit = {
    registerUnbind(deck.on("activate", (e: BespokeEvent) ⇒ {
      sendEvent(BespokeMessage.activate(e.index))
    }))
    registerUnbind(deck.on("deactivate", (e: BespokeEvent) ⇒ {
      sendEvent(BespokeMessage.deactivate(e.index))
    }))
    registerUnbind(deck.on("slide", (e: BespokeEvent) ⇒ {
      sendEvent(BespokeMessage.slide(e.index))
    }))
    registerUnbind(deck.on("next", (e: BespokeEvent) ⇒ {
      sendEvent(BespokeMessage.next(e.index))
    }))
    registerUnbind(deck.on("prev", (e: BespokeEvent) ⇒ {
      sendEvent(BespokeMessage.prev(e.index))
    }))
    open { (ws, event) ⇒ }
  }

  private def sendEvent(m: BespokeMessage): Boolean = {
    sendBytes(Pickle.intoBytes(m))
    true
  }
}
