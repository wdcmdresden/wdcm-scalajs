package de.knutwalker.wdcm

import de.knutwalker.bespoke.{ BespokeEvent, Deck, Plugin }
import boopickle.Default.Unpickle

import scala.scalajs.js
import scala.scalajs.js.typedarray.{ TypedArrayBuffer, ArrayBuffer }

case object Handout extends Plugin with Websockets {
  private[this] val unpickler = Unpickle[BespokeMessage]

  override def apply(deck: Deck): Unit = {
    var active = -1
    var max = 0

    deck.on("slide", (e: BespokeEvent) ⇒ e.index <= max)
    deck.on("next", (e: BespokeEvent) ⇒ e.index < max)
    deck.on("activate", (e: BespokeEvent) ⇒ (e.index <= max) && {
      active = e.index
      true
    })

    open { (ws, wsMsg) ⇒
      val data = TypedArrayBuffer.wrap(wsMsg.data.asInstanceOf[ArrayBuffer])
      val message = unpickler.fromBytes(data)
      message match {
        case BespokeMessage.Next(n)                    ⇒
          max = js.Math.max(n, max)
          if (n != active) deck.slide(n)
          deck.next()
        case BespokeMessage.Prev(n)                    ⇒
          max = js.Math.max(n, max)
          if (n != active) deck.slide(n)
          deck.prev()
        case BespokeMessage.Slide(n) if n != active    ⇒
          max = js.Math.max(n, max)
          deck.slide(n)
        case BespokeMessage.Activate(n) if n != active ⇒
          max = js.Math.max(n, max)
          deck.slide(n)
        case otherwise                                 ⇒
      }
    }
  }
}
