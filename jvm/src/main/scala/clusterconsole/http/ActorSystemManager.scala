package clusterconsole.http

import java.net.ServerSocket

import akka.actor.{ Props, ActorRef, ActorSystem, Actor }
import akka.actor.Actor.Receive
import clusterconsole.core.LogF
import com.typesafe.config.ConfigFactory

import scala.util.{ Success, Failure, Try }

object ActorSystemManager {

  def props(system: String): Props = Props(new ActorSystemManager(system))

}

class ActorSystemManager(system: String) extends Actor with LogF {

  val akkaConf =
    s"""akka.remote.netty.tcp.hostname="127.0.0.1"
      |akka.remote.netty.tcp.port=0
      |akka.cluster.roles = [clusterconsole]
      |""".stripMargin

  val config = ConfigFactory.parseString(akkaConf).withFallback(ConfigFactory.load())

  val newSystem = ActorSystem(system, config)

  val ca: ActorRef = newSystem.actorOf(Props(classOf[ClusterAwareManager]))

  def receive: Receive = {
    case msg: Discover =>
      println("@@@@@@    ActorSystemManager Discover")
      ca ! msg

  }

}
