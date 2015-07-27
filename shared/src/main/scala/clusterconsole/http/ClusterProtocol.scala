package clusterconsole.http

sealed trait ClusterProtocol

trait ClusterEvent extends ClusterProtocol{
  val system:String
}

/*
** EVENTS
 */

case class CurrentClusterStateInitial(system: String, members: Set[ClusterMember]) extends ClusterEvent

case class ClusterUnjoin(system: String, seedNodes: List[HostPort]) extends ClusterEvent

case class ClusterMemberUp(system: String,member: ClusterMember) extends ClusterEvent

case class ClusterMemberUnreachable(system: String,member: ClusterMember) extends ClusterEvent

case class ClusterMemberRemoved(system: String,member: ClusterMember) extends ClusterEvent

case class ClusterMemberExited(system: String,member: ClusterMember) extends ClusterEvent


object ClusterEventUtil{
  def label(e:ClusterEvent) = {
    e match {
      case ev:CurrentClusterStateInitial => s"CurrentClusterStateInitial[ ${ev.system}, ${ev.members}} ]"
      case ev:ClusterUnjoin => s"ClusterUnjoin[ ${ev.system}]"
      case ev:ClusterMemberUp => s"ClusterMemberUp[ ${ev.system} ${ev.member.label}}]"
      case ev:ClusterMemberUnreachable => s"ClusterMemberUnreachable[ ${ev.system} ${ev.member.label}}]"
      case ev:ClusterMemberRemoved => s"ClusterMemberRemoved[ ${ev.system} ${ev.member.label}}]"
      case ev:ClusterMemberExited => s"ClusterMemberExited[ ${ev.system} ${ev.member.label}}]"
    }
  }
}


case class DiscoveryBegun(system: String, seedNodes: List[HostPort]) extends ClusterProtocol

case class DiscoveredCluster(
                              system: String,
                              seeds: List[HostPort],
                              status: String,
                              members: Set[ClusterMember] = Set.empty[ClusterMember]) extends ClusterProtocol

///*
//** COMMANDS
// */
//
//case class Discover(system: String, seedNodes: List[HostPort]) extends ClusterProtocol
//
//case class ClusterSubscribe(name: String) extends ClusterProtocol


/*
** OTHER
 */


case class ClusterMember( address: HostPort, roles:Set[String], state:String) {
  def label = address.label + s" roles[${roles.mkString(",").map(r => r)}] status[$state]"
}

case class HostPort(host: String, port: Int){
  def label = host +":"+port
}

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


