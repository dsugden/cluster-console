package clusterconsole.http

import akka.actor._
import akka.routing.{ ActorRefRoutee, AddRoutee, RemoveRoutee }
import akka.stream.actor.ActorPublisher
import clusterconsole.core.LogF

import scala.annotation.tailrec

class ClusterPublisher(router: ActorRef) extends ActorPublisher[String] with ActorLogging {

  case class QueueUpdated()

  import akka.stream.actor.ActorPublisherMessage._

  import scala.collection.mutable

  val MaxBufferSize = 50
  val queue = mutable.Queue[String]()

  var queueUpdated = false

  // on startup, register with routee
  override def preStart() {
    router ! AddRoutee(ActorRefRoutee(self))
  }

  // cleanly remove this actor from the router. To
  // make sure our custom router only keeps track of
  // alive actors.
  override def postStop(): Unit = {
    router ! RemoveRoutee(ActorRefRoutee(self))
  }

  def receive = {

    // receive new stats, add them to the queue, and quickly
    // exit.
    case clusterEvent: String =>

      // remove the oldest one from the queue and add a new one
      if (queue.size == MaxBufferSize) queue.dequeue()
      queue += clusterEvent
      if (!queueUpdated) {
        queueUpdated = true
        self ! QueueUpdated
      }

    // we receive this message if there are new items in the
    // queue. If we have a demand for messages send the requested
    // demand.
    case QueueUpdated => deliver()

    // the connected subscriber request n messages, we don't need
    // to explicitely check the amount, we use totalDemand propery for this
    case Request(amount) =>
      deliver()

    // subscriber stops, so we stop ourselves.
    case Cancel =>
      context.stop(self)
  }

  /**
   * Deliver the message to the subscriber. In the case of websockets over TCP, note
   * that even if we have a slow consumer, we won't notice that immediately. First the
   * buffers will fill up before we get feedback.
   */
  @tailrec final def deliver(): Unit = {
    if (totalDemand == 0) {
      log.debug(s"No more demand for {}", this)
    }

    if (queue.isEmpty && totalDemand != 0) {
      // we can response to queueupdated msgs again, since
      // we can't do anything until our queue contains stuff again.
      queueUpdated = false
    } else if (totalDemand > 0 && queue.nonEmpty) {
      onNext(queue.dequeue())
      deliver()
    }
  }
}

