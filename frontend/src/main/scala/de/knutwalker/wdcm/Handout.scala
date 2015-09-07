package de.knutwalker.wdcm

import boopickle.Default.Unpickle
import de.knutwalker.bespoke.{ BespokeEvent, Deck, Plugin }
import de.knutwalker.toastr.Toastr
import org.scalajs.dom.Event

import scala.scalajs.js
import scala.scalajs.js.typedarray.{ ArrayBuffer, TypedArrayBuffer }

case object Handout extends Plugin with Websockets {
  private[this] val unpickler = Unpickle[BespokeMessage]

  Toastr.options.progressBar = true
  Toastr.options.preventDuplicates = true
  Toastr.options.timeOut = 15000
  Toastr.options.extendedTimeOut = 30000

  override def apply(deck: Deck): Unit = {
    var previousPresenter = -1
    var active = -1
    var max = 0

    deck.on("slide", (e: BespokeEvent) ⇒ e.index <= max)
    deck.on("next", (e: BespokeEvent) ⇒ e.index < max)
    deck.on("activate", (e: BespokeEvent) ⇒ (e.index <= max) && {
      Toastr.clear()
      active = e.index
      true
    })

    open { (ws, wsMsg) ⇒
      val data = TypedArrayBuffer.wrap(wsMsg.data.asInstanceOf[ArrayBuffer])
      val message = unpickler.fromBytes(data)
      message match {
        case BespokeMessage.Next(n) if n == active ⇒
          max = js.Math.max(n, max)
          deck.next()
        case BespokeMessage.Prev(n) if n == active ⇒
          max = js.Math.max(n, max)
          deck.prev()
        case BespokeMessage.Slide(n) if n != active    ⇒
          max = js.Math.max(n, max)
          Toastr.remove()
          deck.slide(n)
        case BespokeMessage.Activate(n) if n != active ⇒
          max = js.Math.max(n, max)
          val delta = n - previousPresenter
          if (n == active + delta) {
            Toastr.clear()
            deck.slide(n)
          } else toast(active, n) {
            deck.slide(n)
          }
        case BespokeMessage.Deactivate(n) ⇒
          previousPresenter = n
        case BespokeMessage.Connect                    ⇒
          max = 0
          active = 0
          deck.slide(0)
          Toastr.clear()
        case otherwise                                 ⇒
      }
    }
  }

  private def toast(currentlyOn: Int, expectedToBe: Int)(onClick: => Unit): Unit = {
    val msg =
      s"You are on slide $currentlyOn, " +
      s"while the presentation is at slide $expectedToBe. " +
      s"Click this message to go to that slide."

    Toastr.remove()
    Toastr.info(msg, js.undefined, js.Dynamic.literal(
      onclick = { (e: Event) ⇒ onClick }: js.Function1[Event, Unit]
    ))
  }
}
