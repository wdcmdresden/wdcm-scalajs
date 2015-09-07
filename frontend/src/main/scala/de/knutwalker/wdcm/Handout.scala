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

    // block requests to a certain slide if it was not yet seen
    deck.on("slide", (e: BespokeEvent) ⇒ e.index <= max)

    // block requests to the next slide if it was not yet seen
    deck.on("next", (e: BespokeEvent) ⇒ e.index < max)

    // block requests to a certain slide if it was not yet seen
    deck.on("activate", (e: BespokeEvent) ⇒ (e.index <= max) && {
      Toastr.clear()
      active = e.index
      true
    })

    open { (ws, wsMsg) ⇒
      val data = TypedArrayBuffer.wrap(wsMsg.data.asInstanceOf[ArrayBuffer])
      val message = unpickler.fromBytes(data)
      message match {

        // next is only processed if the handout is on the correct slide
        case BespokeMessage.Next(n) if n == active ⇒
          max = js.Math.max(n, max)
          deck.next()

        // prev is only processed if the handout is on the correct slide
        case BespokeMessage.Prev(n) if n == active ⇒
          max = js.Math.max(n, max)
          deck.prev()

        // slide is always processed, as it signals a forced jump by the presenter
        case BespokeMessage.Slide(n) if n != active ⇒
          max = js.Math.max(n, max)
          Toastr.remove()
          deck.slide(n)

        // activate happens when the presenter proceeds to the next slide.
        // it is only processed when the target is not already the current slide
        // and it only applies automatically if the slide would be the next one
        case BespokeMessage.Activate(n) if n != active ⇒
          max = js.Math.max(n, max)
          val delta = n - previousPresenter
          if (n == active + delta) {
            Toastr.clear()
            deck.slide(n)
          } else toast(active, n) {
            deck.slide(n)
          }

        // save which page the presenter moved away from to determine the
        // direction they are going
        case BespokeMessage.Deactivate(n) ⇒
          previousPresenter = n

        // connect signals a new presenter, so reset everything to zero
        case BespokeMessage.Connect                    ⇒
          max = 0
          active = 0
          deck.slide(0)
          Toastr.clear()

        // don't handle other events (and don't fail when they arrive)
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
