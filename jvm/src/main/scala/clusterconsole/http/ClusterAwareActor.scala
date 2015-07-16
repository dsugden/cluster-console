package clusterconsole.http

import akka.actor.{ Actor, ActorRef }
import akka.routing.{ RemoveRoutee, ActorRefRoutee, AddRoutee }
import clusterconsole.core.LogF

import Json._

class ClusterAwareActor(router: ActorRef) extends Actor with LogF {

  override def preStart() {
    router ! AddRoutee(ActorRefRoutee(self))
  }

  override def postStop(): Unit = {
    router ! RemoveRoutee(ActorRefRoutee(self))
  }

  def receive: Receive = {
    case x: ClusterProtocol =>
      x.logDebug("--- ClusterProtocol: " + _)
      router ! upickle.write(x)
  }

}
