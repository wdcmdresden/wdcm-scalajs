package de.knutwalker.toastr

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

@JSName("toastr")
@js.native
object Toastr extends js.Object {
  def success(message: String, title: js.UndefOr[String] = js.native, overrideOptions: js.UndefOr[js.Dynamic] = js.native): Unit = js.native
  def info(message: String, title: js.UndefOr[String] = js.native, overrideOptions: js.UndefOr[js.Dynamic] = js.native): Unit = js.native
  def warning(message: String, title: js.UndefOr[String] = js.native, overrideOptions: js.UndefOr[js.Dynamic] = js.native): Unit = js.native
  def error(message: String, title: js.UndefOr[String] = js.native, overrideOptions: js.UndefOr[js.Dynamic] = js.native): Unit = js.native

  def remove(): Unit = js.native
  def clear(): Unit = js.native

  def options: js.Dynamic = js.native
}
