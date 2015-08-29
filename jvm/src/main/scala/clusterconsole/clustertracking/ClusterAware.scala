package clusterconsole.clustertracking

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{ Cluster, Member }
import clusterconsole.core.LogF
import clusterconsole.http._
import com.typesafe.config.ConfigFactory

import scala.collection.immutable

case class IsDiscovered(system: DiscoveredCluster)

object ClusterAware {

  def props(systemName: String, selfHost: String, seedNodes: List[HostPort], parent: ActorRef): Props =
    Props(new ClusterAware(systemName, selfHost, seedNodes, parent))

  def toClusterMember(m: Member, nodeState: NodeState): ClusterMember =
    ClusterMember(
      HostPort(m.uniqueAddress.address.host.getOrElse("Unknown"), m.uniqueAddress.address.port.getOrElse(0)),
      m.roles,
      nodeState
    )

}

class ClusterAware(systemName: String,
    selfHost: String,
    seedNodes: List[HostPort],
    parent: ActorRef) extends Actor with ActorLogging {

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

  /** subscribe to cluster events in order to track workers */
  override def preStart() = {
    val addresses: immutable.Seq[Address] =
      seedNodes.map(e => Address("akka.tcp", systemName, e.host, e.port))

    // todo - track cluster metrics
    cluster.subscribe(self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberUp],
      classOf[UnreachableMember],
      classOf[MemberRemoved],
      classOf[MemberExited],
      classOf[LeaderChanged]
    )

    cluster.joinSeedNodes(addresses)

  }

  override def postStop() = {
    cluster.unsubscribe(self)
    cluster.leave(
      Address("akka.tcp", systemName, selfHost, selfPort)
    )
  }

  def receive: Receive = trackingMembers(Set.empty[Member])

  def trackingMembers(members: Set[Member]): Receive = {

    case m @ CurrentClusterState(clusterMembers, _, _, _, _) =>
      context.become(
        trackingMembers(clusterMembers)
      )

    case MemberUp(m) =>
      // ignore ourself for console view
      if (m.roles != Set("clusterconsole")) {
        context.become(
          trackingMembers(members + m)
        )
        def clusterMember(m: Member) =
          ClusterAware.toClusterMember(m, Up)

        parent ! ClusterMemberUp(systemName, clusterMember(m))

        parent ! IsDiscovered(
          DiscoveredCluster(systemName, seedNodes, "", (members + m).map(clusterMember), Seq.empty[RoleDependency])
        )
      }

    case UnreachableMember(m) =>
      if (m.roles != Set("clusterconsole"))
        parent ! ClusterMemberUnreachable(systemName, ClusterAware.toClusterMember(m, Unreachable))

    case MemberRemoved(m, previousStatus) =>
      if (m.roles != Set("clusterconsole")) {
        parent ! ClusterMemberRemoved(systemName, ClusterAware.toClusterMember(m, Removed))
      }

    case MemberExited(m) =>
      if (m.roles != Set("clusterconsole")) {
        parent ! ClusterMemberExited(systemName, ClusterAware.toClusterMember(m, Exited))
      }

  }

}
