package de.knutwalker.wdcm

import akka.actor.ActorSystem
import akka.http.ServerSettings
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.util.{ Failure, Success }

object Boot {
  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val appSettings    = AppSettings(system.settings.config)
  val serverSettings = ServerSettings(system).copy(maxConnections = 128)

  def main(args: Array[String]): Unit = {
    import appSettings._
    import system.dispatcher
    val httpExt = Http()

    val handoutSystem = HandoutSystem()
    val presenterService = new PresenterService(appSettings, handoutSystem)
    httpExt.bindAndHandle(presenterService.route, presenterInterface, presenterPort) onComplete {
      case Success(presenter) ⇒
        presenterService.addBinding(presenter)
        val presenterAddress = presenter.localAddress
        println(s"Presenter server is listening on ${presenterAddress.getHostName}:${presenterAddress.getPort}")

        val handoutService = new HandoutService(appSettings, handoutSystem).route
        httpExt.bindAndHandle(handoutService, handoutInterface, handoutPort) onComplete {
          case Success(handout) ⇒
            presenterService.addBinding(handout)
            val handoutAddress = handout.localAddress
            println(s"Handout server is listening on ${handoutAddress.getHostName}:${handoutAddress.getPort}")

          case Failure(ex) ⇒
            system.log.error(ex, "Handout binding failed, shutting down")
            presenter.unbind().onComplete(_ ⇒ system.shutdown())
        }

      case Failure(ex) ⇒
        system.log.error(ex, "Presenter binding failed, shutting down")
        system.shutdown()
    }
  }
}
