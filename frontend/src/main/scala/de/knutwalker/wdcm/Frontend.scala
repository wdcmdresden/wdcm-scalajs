package de.knutwalker.wdcm

import boopickle.Default.Unpickle
import de.knutwalker.bespoke.Bespoke
import org.scalajs.dom

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.typedarray.{ ArrayBuffer, TypedArrayBuffer }

object Frontend extends js.JSApp {
  def main(): Unit = {
    val plugins = Bespoke.plugins
    val firstPlugins = js.Array(Bespoke.themes.voltaire(), plugins.keys(), plugins.touch())
    val lastPlugins = js.Array(plugins.scale(), plugins.hash(), plugins.progress())

    dom.ext.Ajax.get("identify", responseType = "arraybuffer").map { xhr ⇒
      val data = xhr.response.asInstanceOf[ArrayBuffer]
      val id = Unpickle[ServiceIdentity].fromBytes(TypedArrayBuffer.wrap(data))
      val presentationPlugins = id match {
        case ServiceIdentity.Presenter ⇒
          println(s"identified as presenter")
          js.Array(Presenter.asPlugin(), plugins.bullets("li, .bullet"))
        case ServiceIdentity.Handout   ⇒
          println(s"identified as handout")
          js.Array(plugins.bullets("li, .bullet"), Handout.asPlugin())
      }
      firstPlugins.concat(presentationPlugins).concat(lastPlugins)
    }.onSuccess { case finalPlugins ⇒ Bespoke.from("article", finalPlugins) }
  }
}
