package clusterconsole.http

sealed trait ClusterProtocol

trait ClusterEvent extends ClusterProtocol

/*
** EVENTS
 */

case class CurrentClusterStateInitial(members: Set[ClusterMember]) extends ClusterEvent

case class ClusterUnjoin(system: String, seedNodes: List[HostPort]) extends ClusterEvent

case class ClusterMemberUp(member: ClusterMember) extends ClusterEvent

case class ClusterMemberUnreachable(member: ClusterMember) extends ClusterEvent

case class ClusterMemberRemoved(member: ClusterMember) extends ClusterEvent

case class ClusterMemberExited(member: ClusterMember) extends ClusterEvent

case class DiscoveryBegun(system: String, seedNodes: List[HostPort]) extends ClusterEvent

case class Discovered(system: String) extends ClusterEvent

case class DiscoveredCluster(
                              name: String,
                              seeds: List[HostPort],
                              status: String,
                              members: Set[ClusterMember] = Set.empty[ClusterMember]) extends ClusterEvent

/*
** COMMANDS
 */

case class Discover(system: String, seedNodes: List[HostPort]) extends ClusterProtocol

case class ClusterSubscribe(name: String) extends ClusterProtocol


/*
** OTHER
 */

case class ClusterMember( address: HostPort, roles:Set[String], state:String)

case class HostPort(host: String, port: Int)

object HostPortUtil {
  def apply(hp: HostPortForm): HostPort =
    HostPort(hp.host,
      try {
        hp.port.toInt
      } catch {
        case e: Throwable => 0
      }
    )
}

case class HostPortForm(host: String, port: String)


case class ClusterForm(name: String, seeds: List[HostPortForm])

object ClusterForm {
  def initial: ClusterForm = ClusterForm("", List(HostPortForm("", "")))
}


