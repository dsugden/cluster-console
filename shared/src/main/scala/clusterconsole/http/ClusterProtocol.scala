package clusterconsole.http

sealed trait ClusterProtocol

case class ClusterMemberUp(clusterName: String, member: String) extends ClusterProtocol


case class ClusterMember(name: String)


case class ClusterSubscribe(name: String) extends ClusterProtocol

case class TestResponse(v: String) extends ClusterProtocol




case class DiscoveredCluster(name:String, seeds:List[String], members:Seq[ClusterMember] = Nil)


