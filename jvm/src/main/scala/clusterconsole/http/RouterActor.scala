package clusterconsole.http

import akka.actor.Actor
import akka.routing.{ RemoveRoutee, AddRoutee, Routee }
import clusterconsole.core.LogF

class RouterActor extends Actor with LogF {
  var routees = Set[Routee]()

  def receive = {
    case ar: AddRoutee => routees = routees + ar.routee
    case rr: RemoveRoutee => routees = routees - rr.routee
    case msg =>

      routees.foreach { r =>

        r.logDebug("---------- RouterActor routee " + _)
        msg.logDebug("sending " + _)
        r.send(msg, sender)
      }
  }
}