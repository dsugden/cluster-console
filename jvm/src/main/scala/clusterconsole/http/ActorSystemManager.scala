package clusterconsole.http

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.typesafe.config.ConfigFactory

import scala.collection.immutable

object ActorSystemManager {

  def props(
    systemName: String,
    seedNodes: List[HostPort],
    socketPublisherRouter: ActorRef): Props = Props(new ActorSystemManager(systemName, seedNodes, socketPublisherRouter))

}

class ActorSystemManager(systemName: String, seedNodes: List[HostPort], socketPublisherRouter: ActorRef) extends Actor {

  val selfHost = "127.0.0.1"
  val selfPort = 0

  val akkaConf =
    s"""akka.remote.netty.tcp.hostname="$selfHost"
      |akka.remote.netty.tcp.port=$selfPort
      |auto-down-unreachable-after = 5s
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

    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberUp], classOf[UnreachableMember], classOf[MemberRemoved])

  }

  override def postStop() = {
    println("@@@@@@    ActorSystemManager postStop")
    cluster.unsubscribe(self)
    cluster.leave(Address("akka.tcp", systemName, selfHost, selfPort))
  }

  def receive: Receive = {
    case msg: MemberUp => socketPublisherRouter ! ClusterMemberUp(msg.member.address.toString)
    case msg: UnreachableMember => socketPublisherRouter ! ClusterMemberUnreachable(msg.member.address.toString)
    case msg: MemberRemoved => socketPublisherRouter ! ClusterMemberRemoved(msg.member.address.toString)
  }

}
