package clusterconsole.http

sealed trait ClusterProtocol



sealed trait ClusterEvent extends ClusterProtocol {
  val clusterName: String
}

case class ClusterMemberUp(clusterName: String, member: String) extends ClusterEvent


case class ClusterMember(name: String)


sealed trait ClusterRequest extends ClusterProtocol

case class TestMessage(v: String) extends ClusterRequest


case class TestResponse(v: String) extends ClusterRequest


