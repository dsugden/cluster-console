package clusterconsole.http

import akka.actor.{ Props, ActorRef, ActorSystem, Actor }
import akka.actor.Actor.Receive
import com.typesafe.config.ConfigFactory

object ActorSystemManager {

  def props(system: String): Props = Props(new ActorSystemManager(system))

}

class ActorSystemManager(system: String) extends Actor {

  val akkaConf =
    """akka.remote.netty.tcp.hostname="127.0.0.1"
      |akka.remote.netty.tcp.port=2881
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
