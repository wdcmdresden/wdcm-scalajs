package de.knutwalker.wdcm

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.NoContent
import akka.http.scaladsl.model.ws.{ BinaryMessage, Message, TextMessage }
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import boopickle.Default.{ Pickle, Unpickle }

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

class PresenterService(val appSettings: AppSettings, handout: HandoutSystem)(implicit system: ActorSystem, mat: Materializer) extends StaticRoutes {
  private[this] val bindings  = new AtomicReference[List[ServerBinding]](Nil)
  private[this] val unpickler = Unpickle[BespokeMessage]

  @tailrec
  final def addBinding(binding: ServerBinding): Boolean = {
    val before = bindings.get()
    val after = binding :: before
    bindings.compareAndSet(before, after) || addBinding(binding)
  }

  val route =
    path("ws") {
      handleWebsocketMessages(presenterFlow)
    } ~
    path("identify") {
      complete(ByteString(Pickle.intoBytes(ServiceIdentity.presenter)))
    } ~
    path("_shutdown") {
      post {
        complete {
          import system.dispatcher
          val unbound = bindings.get().map(_.unbind())
          Future.sequence(unbound).onComplete(_ ⇒ system.scheduler.scheduleOnce(250 millis)(system.shutdown()))
          HttpResponse(NoContent)
        }
      }
    } ~
    statics

  private def presenterFlow: Flow[Message, Message, Unit] =
    Flow[Message].collect {
      case TextMessage.Strict(msg)     ⇒
        unpickler.fromBytes(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)))
      case BinaryMessage.Strict(bytes) ⇒
        unpickler.fromBytes(bytes.asByteBuffer)
    }
    .via(handout.presenterFlow)
    .map(_ ⇒ TextMessage(""))


}
