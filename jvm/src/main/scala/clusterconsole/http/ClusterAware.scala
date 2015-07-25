package clusterconsole.http

import akka.actor._
import akka.cluster.{ Member, Cluster }
import akka.cluster.ClusterEvent._
import clusterconsole.core.LogF
import com.typesafe.config.ConfigFactory

import scala.collection.immutable

object ClusterAware {

  def props(
    systemName: String,
    seedNodes: List[HostPort],
    socketPublisherRouter: ActorRef): Props = Props(new ClusterAware(systemName, seedNodes, socketPublisherRouter))

  def toCusterMember(m: Member): ClusterMember =
    ClusterMember(
      HostPort(m.uniqueAddress.address.host.getOrElse("Unknown"), m.uniqueAddress.address.port.getOrElse(0)),
      m.roles,
      m.status.toString)

}

class ClusterAware(systemName: String, seedNodes: List[HostPort], socketPublisherRouter: ActorRef) extends Actor with LogF {

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

    cluster.subscribe(self, initialStateMode = InitialStateAsSnapshot,
      classOf[MemberUp], classOf[UnreachableMember],
      classOf[MemberRemoved], classOf[MemberExited], classOf[LeaderChanged])

  }

  override def postStop() = {
    println("@@@@@@    ActorSystemManager postStop")
    cluster.unsubscribe(self)
    cluster.leave(Address("akka.tcp", systemName, selfHost, selfPort))
  }

  def receive: Receive = {

    case CurrentClusterState(members, _, _, _, _) =>
      members.logDebug("------- CurrentClusterState: " + _)
      socketPublisherRouter ! CurrentClusterStateInitial(members.map(ClusterAware.toCusterMember))

    case MemberUp(m) =>
      m.logDebug("------- MemberUp: " + _)
      socketPublisherRouter ! ClusterMemberUp(ClusterAware.toCusterMember(m))

    case msg: UnreachableMember =>
      msg.logDebug("------- msg: " + _)
      socketPublisherRouter ! ClusterMemberUnreachable(msg.member.address.toString)

    case msg: MemberRemoved =>
      socketPublisherRouter ! ClusterMemberRemoved(msg.member.address.toString)

    case msg: MemberExited =>
      socketPublisherRouter ! ClusterMemberExited(msg.member.address.toString)

  }

}
