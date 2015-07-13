package samplecluster

import akka.actor.{ Props, Actor }
import akka.actor.Actor.Receive

object BackEnd {
  def props: Props = Props(new BackEnd)
}

class BackEnd extends Actor {
  def receive: Receive = {

    case _ =>

  }
}
