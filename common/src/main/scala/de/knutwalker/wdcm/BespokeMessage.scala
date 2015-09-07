package de.knutwalker.wdcm

import boopickle.Default._

sealed trait BespokeMessage extends Product with Serializable
object BespokeMessage {
  def deactivate(slide: Int): BespokeMessage = Deactivate(slide)
  def activate(slide: Int): BespokeMessage = Activate(slide)
  def slide(slide: Int): BespokeMessage = Slide(slide)
  def next(slide: Int): BespokeMessage = Next(slide)
  def prev(slide: Int): BespokeMessage = Prev(slide)
  def disconnect: BespokeMessage = Disconnect

  case class Deactivate(slide: Int) extends BespokeMessage
  case class Activate(slide: Int) extends BespokeMessage
  case class Slide(slide: Int) extends BespokeMessage
  case class Next(slide: Int) extends BespokeMessage
  case class Prev(slide: Int) extends BespokeMessage
  case object Disconnect extends BespokeMessage

  implicit val picklerBespokeMessage: Pickler[BespokeMessage] =
    generatePickler[BespokeMessage]
}
