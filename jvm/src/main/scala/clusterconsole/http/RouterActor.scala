package clusterconsole.http

import akka.actor.Actor
import akka.routing.{ RemoveRoutee, AddRoutee, Routee }
import clusterconsole.core.LogF

class RouterActor extends Actor with LogF {
  var routees = Set[Routee]()

  def receive = {
    case ar: AddRoutee => routees = routees + ar.routee
    case rr: RemoveRoutee => routees = routees - rr.routee
    case msg: ClusterProtocol =>
      routees.foreach { r =>
        msg.logDebug("sending " + _)
        import Json._
        r.send(upickle.write(msg), sender)
      }
  }
}