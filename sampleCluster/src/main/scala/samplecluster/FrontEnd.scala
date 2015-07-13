package samplecluster

import akka.actor.{ Props, Actor }
import akka.actor.Actor.Receive

object FrontEnd {
  def props: Props = Props(new FrontEnd)
}

class FrontEnd extends Actor {
  def receive: Receive = {
    case _ =>
  }
}
