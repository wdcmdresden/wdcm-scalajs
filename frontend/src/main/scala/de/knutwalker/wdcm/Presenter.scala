package de.knutwalker.wdcm

import de.knutwalker.bespoke.{ BespokeEvent, Deck, Plugin }
import org.scalajs.dom.Event
import org.scalajs.dom.raw.WebSocket
import boopickle.Default.Pickle

case object Presenter extends Plugin with Websockets {

  override protected def onOpen(ws: WebSocket, event: Event): Unit =
    sendEvent(BespokeMessage.connect)

  override protected def onClose(ws: WebSocket, event: Event): Unit =
    sendEvent(BespokeMessage.disconnect)

  override def apply(deck: Deck): Unit = {
    // Need to register before opening, otherwise we lose the race for handling
    // 'next' and 'prev' against the bullet plugin, which swallows next events
    // in order to open its bullets, but we could not inform the handout clients
    // to jump to their next bullet item as well.
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
    send(Pickle.intoBytes(m))
    true
  }
}
