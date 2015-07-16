package clusterconsole.http

import akka.actor.{ Address, Actor, ActorSystem, Props }
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{ MemberRemoved, MemberExited, MemberUp }
import com.typesafe.config.ConfigFactory

import scala.collection.immutable

object ActorSystemManager {

  def props(systemName: String, seedNodes: List[HostPort]): Props = Props(new ActorSystemManager(systemName, seedNodes))

}

class ActorSystemManager(systemName: String, seedNodes: List[HostPort]) extends Actor {

  val selfHost = "127.0.0.1"
  val selfPort = 0 //2881

  val akkaConf =
    s"""akka.remote.netty.tcp.hostname="$selfHost"
      |akka.remote.netty.tcp.port=$selfPort
      |akka.cluster.roles = [clusterconsole]
      |""".stripMargin

  val config = ConfigFactory.parseString(akkaConf).withFallback(ConfigFactory.load())

  lazy val newSystem = ActorSystem(systemName, config)

  lazy val cluster = Cluster(newSystem)

  //val ca = newSystem.actorOf(Props(classOf[ClusterAwareManager]))

  /** subscribe to cluster event in order to track workers */
  override def preStart() = {
    println("@@@@@@ lazy!!!!!!    ActorSystemManager preStart")
    val addresses: immutable.Seq[Address] = seedNodes.map(e => Address("akka.tcp", systemName, e.host, e.port))
    cluster.joinSeedNodes(addresses)
    cluster.subscribe(self, classOf[MemberUp])
    cluster.subscribe(self, classOf[MemberExited])
    cluster.subscribe(self, classOf[MemberRemoved])
  }

  override def postStop() = {
    println("@@@@@@    ActorSystemManager postStop")
    cluster.unsubscribe(self)
    cluster.leave(Address("akka.tcp", systemName, selfHost, selfPort))
  }

  def receive: Receive = {
    case _ =>
  }

}
