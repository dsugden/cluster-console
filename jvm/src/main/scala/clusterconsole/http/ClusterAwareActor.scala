package clusterconsole.http

import akka.actor.{ Actor, ActorRef }
import clusterconsole.core.LogF

import Json._

class ClusterAwareActor(router: ActorRef) extends Actor with LogF {

  def receive: Receive = {
    case x: ClusterProtocol =>
      x.logDebug("--- ClusterProtocol: " + _)
      router ! upickle.write(x)
  }

}
