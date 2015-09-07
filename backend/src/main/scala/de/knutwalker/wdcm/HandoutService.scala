package de.knutwalker.wdcm

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{ BinaryMessage, Message }
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import boopickle.Default.Pickle

import scala.language.postfixOps

final class HandoutService(val appSettings: AppSettings, handout: HandoutSystem)(implicit system: ActorSystem, mat: Materializer) extends StaticRoutes {

  val route =
    path("ws") {
      handleWebsocketMessages(handoutFlow)
    } ~
    path("identify") {
      complete(ByteString(Pickle.intoBytes(ServiceIdentity.handout)))
    } ~
    statics

  private def handoutFlow: Flow[Message, Message, Unit] =
    Flow[Message].via(handout.handoutFlow).map { msg ⇒
      BinaryMessage.Strict(ByteString(Pickle.intoBytes(msg)))
    }
}
