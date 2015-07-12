package clusterconsole.cluster

import akka.actor.{ ActorRef, Actor }
import clusterconsole.core.LogF
import clusterconsole.http.{ ClusterMemberUp, ClusterEvent }

class ClusterAwareActor(router: ActorRef) extends Actor with LogF {

  def receive: Receive = {
    case x: ClusterMemberUp =>
      x.logDebug("--- ClusterAwareActor ClusterEvent: " + _)
      router ! upickle.write(x)
  }

}
