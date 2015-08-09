package clusterconsole.http

import akka.actor.Actor
import akka.routing.{ RemoveRoutee, AddRoutee, Routee }
import clusterconsole.core.LogF
import upickle.default._

class RouterActor extends Actor with LogF {
  var routees = Set[Routee]()

  def receive = {
    case ar: AddRoutee => routees = routees + ar.routee
    case rr: RemoveRoutee => routees = routees - rr.routee
    case msg: ClusterProtocol =>
      routees.foreach { r =>
        import Json._
        r.send(write(msg), sender)
      }
  }
}