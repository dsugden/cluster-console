package clusterconsole.http

sealed trait ClusterProtocol

case class ClusterMemberUp(clusterName: String, member: String) extends ClusterProtocol


case class ClusterMember(name: String)


case class ClusterSubscribe(name: String) extends ClusterProtocol

case class TestResponse(v: String) extends ClusterProtocol

case class HostPort(host: String, port: Int)

case class Discover(system: String, seedNodes: List[HostPort])

case class Discovered(system: String)
case class DiscoveryBegun(system: String, seedNodes: List[HostPort])



case class DiscoveredCluster(name:String, seeds:List[HostPort], members:Seq[ClusterMember] = Nil)


