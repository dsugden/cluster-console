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

  def props(
    systemName: String,
    seedNodes: List[HostPort],
    parent: ActorRef): Props =
    Props(new ClusterAware(systemName, seedNodes, parent))

  def toCusterMember(m: Member, nodeState: NodeState): ClusterMember =
    ClusterMember(
      HostPort(m.uniqueAddress.address.host.getOrElse("Unknown"), m.uniqueAddress.address.port.getOrElse(0)),
      m.roles,
      nodeState)

}

class ClusterAware(systemName: String, seedNodes: List[HostPort], parent: ActorRef) extends Actor with LogF {

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

    cluster.subscribe(self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberUp], classOf[UnreachableMember],
      classOf[MemberRemoved], classOf[MemberExited], classOf[LeaderChanged])

    cluster.joinSeedNodes(addresses)

  }

  override def postStop() = {
    println("@@@@@@    ActorSystemManager postStop")
    cluster.unsubscribe(self)
    cluster.leave(Address("akka.tcp", systemName, selfHost, selfPort))
  }

  def receive: Receive = trackingMembers(Set.empty[Member])

  def trackingMembers(members: Set[Member]): Receive = {

    case m @ CurrentClusterState(members, _, _, _, _) =>
      m.logDebug("------- CurrentClusterState: " + _)
      context.become(trackingMembers(members))
    //      parent ! CurrentClusterStateInitial(systemName, members.map(m => ClusterAware.toCusterMember(m)))
    //      parent ! IsDiscovered(DiscoveredCluster(systemName, seedNodes, "", members.map(m => ClusterAware.toCusterMember(m))))

    case MemberUp(m) =>
      m.logDebug("------- MemberUp: " + _)
      if (m.roles != Set("clusterconsole")) {
        context.become(trackingMembers(members + m))
        def clusterMember(m: Member) = ClusterAware.toCusterMember(m, Up)
        parent ! ClusterMemberUp(systemName, clusterMember(m))
        parent ! IsDiscovered(DiscoveredCluster(systemName, seedNodes, "", (members + m).map(clusterMember)))
      }

    case UnreachableMember(m) =>
      if (m.roles != Set("clusterconsole"))
        parent ! ClusterMemberUnreachable(systemName, ClusterAware.toCusterMember(m, Unreachable))

    case MemberRemoved(m, previousStatus) =>
      if (m.roles != Set("clusterconsole")) {
        parent ! ClusterMemberRemoved(systemName, ClusterAware.toCusterMember(m, Removed))
      }

    case MemberExited(m) =>
      if (m.roles != Set("clusterconsole")) {
        parent ! ClusterMemberExited(systemName, ClusterAware.toCusterMember(m, Exited))
        //        parent ! IsDiscovered(DiscoveredCluster(systemName, seedNodes, "", (members - m).map(m => ClusterAware.toCusterMember(m, Exited))))
      }

  }

}
