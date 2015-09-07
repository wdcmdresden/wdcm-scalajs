package de.knutwalker.bespoke

import org.scalajs.dom.Element

import scala.scalajs.js
import scala.scalajs.js.`|`
import scala.scalajs.js.annotation.{ JSExportDescendentObjects, JSName }

@JSName("bespoke")
@js.native
object Bespoke extends js.Any {
  def from(selectorOrElement: String | Element, plugins: js.Array[js.Function1[Deck, Unit]]): Deck = js.native
  def plugins: PluginRegistry = js.native
  def themes: ThemeRegistry = js.native
}

@js.native
trait Deck extends js.Object {
  val parent: Element = js.native
  def slides: js.Array[Element] = js.native

  def on(eventName: String, callback: js.Function1[BespokeEvent, Boolean]): js.Function0[Unit] = js.native

  def slide(): Int = js.native
  def slide(index: Int): Unit = js.native

  def next(): Unit = js.native
  def prev(): Unit = js.native
}

@js.native
trait BespokeEvent extends js.Object {
  val index: Int     = js.native
  val slide: Element = js.native
}

@js.native
trait PluginRegistry extends js.Object {
  def keys: js.Function0[js.Function1[Deck, Unit]] = js.native
  def touch: js.Function0[js.Function1[Deck, Unit]] = js.native
  def bullets: js.Function1[String, js.Function1[Deck, Unit]] = js.native
  def scale: js.Function0[js.Function1[Deck, Unit]] = js.native
  def hash: js.Function0[js.Function1[Deck, Unit]] = js.native
  def progress: js.Function0[js.Function1[Deck, Unit]] = js.native
}

@js.native
trait ThemeRegistry extends js.Object {
  def voltaire: js.Function0[js.Function1[Deck, Unit]] = js.native
}

@JSExportDescendentObjects
trait Plugin extends (Deck ⇒ Unit) {
  def apply(deck: Deck): Unit
  def asPlugin(): js.Function1[Deck, Unit] = this
}
