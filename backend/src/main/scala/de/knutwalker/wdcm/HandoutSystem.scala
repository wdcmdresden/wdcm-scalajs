package de.knutwalker.wdcm

import akka.actor.{ Actor, ActorLogging, ActorSystem, Terminated }
import akka.event.LoggingReceive
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import de.knutwalker.akka.typed._

trait HandoutSystem {
  def handoutFlow: Flow[Any, BespokeMessage, Unit]
  def presenterFlow: Flow[BespokeMessage, Any, Unit]
}

object HandoutSystem {
  private case class NewViewer(ref: ActorRef[BespokeMessage])

  def apply()(implicit system: ActorSystem): HandoutSystem = {
    val handoutActor = ActorOf(Props[NewViewer, HandoutActor])

    def receiveIncoming: Sink[BespokeMessage, Unit] =
      Sink.actorRef[BespokeMessage](handoutActor.asInstanceOf[UntypedActorRef], BespokeMessage.Disconnect)

    def sendOutgoing: Source[BespokeMessage, Unit] =
      Source.actorRef[BespokeMessage](10, OverflowStrategy.dropTail)
      .mapMaterializedValue(ref ⇒ handoutActor ! NewViewer(ref.asInstanceOf[ActorRef[BespokeMessage]]))

    new HandoutSystem {
      def handoutFlow: Flow[Any, BespokeMessage, Unit] =
        Flow.wrap(Sink.ignore, sendOutgoing)(Keep.none)

      def presenterFlow: Flow[BespokeMessage, Any, Unit] =
        Flow.wrap(receiveIncoming, Source.lazyEmpty)(Keep.none)
    }
  }

  private final class HandoutActor extends Actor with ActorLogging {
    private[this] var viewers  = Set.empty[ActorRef[BespokeMessage]]
    private[this] var sequence = Vector.empty[BespokeMessage]

    def receive: Receive = LoggingReceive {
      case NewViewer(ref)                 ⇒
        context watch ref.asInstanceOf[UntypedActorRef]
        viewers += ref
        log.info(s"A new viewer joined, total of ${viewers.size } now")
        log.info(s"Replaying ${sequence.size } messages")
        sequence.foreach(ref ! _)
      case Terminated(ref)                ⇒
        viewers -= ref.asInstanceOf[ActorRef[BespokeMessage]]
        log.info(s"A viewer left, total of ${viewers.size } now")
      case m: BespokeMessage.Activate     ⇒
        sequence :+= m
        viewers.foreach(_ ! m)
      case m: BespokeMessage.Slide        ⇒
        sequence :+= m
        viewers.foreach(_ ! m)
      case m: BespokeMessage.Prev         ⇒
        sequence :+= m
        viewers.foreach(_ ! m)
      case m: BespokeMessage.Next         ⇒
        sequence :+= m
        viewers.foreach(_ ! m)
      case m@BespokeMessage.Deactivate(n) ⇒
        sequence :+= m
        viewers.foreach(_ ! m)
      case BespokeMessage.Connect         ⇒
        sequence = Vector(BespokeMessage.Connect)
        log.info(s"Presenter joined, telling ${viewers.size } viewers to get ready")
        viewers.foreach(_ ! BespokeMessage.Connect)
      case BespokeMessage.Disconnect      ⇒
        sequence = Vector()
        log.info(s"Presenter left, disconnecting from ${viewers.size } viewers")
        viewers.foreach(_ ! BespokeMessage.Disconnect)
    }
  }
}
