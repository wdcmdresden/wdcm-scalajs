package de.knutwalker.wdcm

import boopickle.Default._

sealed trait ServiceIdentity extends Product with Serializable
object ServiceIdentity {
  def handout: ServiceIdentity = Handout
  def presenter: ServiceIdentity = Presenter

  case object Handout extends ServiceIdentity
  case object Presenter extends ServiceIdentity

  implicit val picklerServiceIdentity: Pickler[ServiceIdentity] =
    generatePickler[ServiceIdentity]
}
