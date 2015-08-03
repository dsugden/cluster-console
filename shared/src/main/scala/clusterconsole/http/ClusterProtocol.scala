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
      case ev:ClusterMemberUp => s"MemberUp ${ev.system} ${ev.member.labelSimple}"
      case ev:ClusterMemberUnreachable => s"MemberUnreachable ${ev.system} ${ev.member.labelSimple}"
      case ev:ClusterMemberRemoved => s"MemberRemoved ${ev.system} ${ev.member.labelSimple}"
      case ev:ClusterMemberExited => s"MemberExited ${ev.system} ${ev.member.labelSimple}"
    }
  }
}


case class DiscoveryBegun(system: String, seedNodes: List[HostPort]) extends ClusterProtocol

case class DiscoveredCluster(
                              system: String,
                              seeds: List[HostPort],
                              status: String,
                              members: Set[ClusterMember] = Set.empty[ClusterMember],
                              dependencies: Seq[RoleDependency] = Seq.empty[RoleDependency]) extends ClusterProtocol {

  def getRoles:Seq[String] = members.foldLeft[Set[String]](Set.empty[String])((a,b) => b.roles ++ a ).toSeq

  def getNodesByRole(role:String) = members.filter(_.roles.contains(role))

}

case class ClusterMember( address: HostPort, roles:Set[String], state:NodeState) {
  def label = address.label + s" roles[${roles.mkString(",").map(r => r)}] status[$state]"
  def labelSimple = address.label
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

case class RoleDependency(roles:Seq[String], dependsOn:Seq[String], tpe:ClusterDependency)

case class HostPortForm(host: String, port: String)

case class ClusterForm(name: String, seeds: List[HostPortForm])

object ClusterForm {
  def initial: ClusterForm = ClusterForm("", List(HostPortForm("", "")))
}


