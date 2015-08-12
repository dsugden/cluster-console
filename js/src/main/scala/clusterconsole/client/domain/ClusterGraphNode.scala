package clusterconsole.client.domain

import clusterconsole.client.d3.Layout.GraphNode
import clusterconsole.http.ClusterMember

import scala.scalajs.js

trait ClusterGraphNode extends GraphNode {
  var host: String = js.native
  var port: Int = js.native
  var roles: String = js.native
  var status: String = js.native

}

object ClusterGraphNode {

  def label(n: ClusterGraphNode): String = n.host + ":" + n.port

  def apply(m: ClusterMember,
    index: Double,
    x: Double,
    y: Double,
    px: Double,
    py: Double,
    fixed: Boolean,
    weight: Double): ClusterGraphNode =
    js.Dynamic.literal(
      "host" -> m.address.host,
      "port" -> m.address.port,
      "roles" -> m.roles.mkString(","),
      "status" -> m.state.toString,
      "name" -> m.label,
      "index" -> index,
      "x" -> x,
      "y" -> y,
      "px" -> px,
      "py" -> py,
      "fixed" -> fixed,
      "weight" -> weight
    ).asInstanceOf[ClusterGraphNode]

  def host(m: ClusterMember,
    index: Double,
    x: Double,
    y: Double,
    px: Double,
    py: Double,
    fixed: Boolean,
    weight: Double): ClusterGraphNode =
    js.Dynamic.literal(
      "host" -> m.address.host,
      "port" -> 0,
      "roles" -> "",
      "status" -> "",
      "name" -> "",
      "index" -> index,
      "x" -> x,
      "y" -> y,
      "px" -> px,
      "py" -> py,
      "fixed" -> fixed,
      "weight" -> weight
    ).asInstanceOf[ClusterGraphNode]

  def port(m: ClusterMember,
    index: Double,
    x: Double,
    y: Double,
    px: Double,
    py: Double,
    fixed: Boolean,
    weight: Double): ClusterGraphNode =
    js.Dynamic.literal(
      "host" -> m.address.host,
      "port" -> m.address.port,
      "roles" -> m.roles.mkString(","),
      "status" -> m.state.toString,
      "name" -> "",
      "index" -> index,
      "x" -> x,
      "y" -> y,
      "px" -> px,
      "py" -> py,
      "fixed" -> fixed,
      "weight" -> weight
    ).asInstanceOf[ClusterGraphNode]

}

trait NodeLike[T] {
  def nodeEq(x: T, y: T): Boolean
}
object NodeLike {

  implicit object ClusterGraphNodeLike extends NodeLike[ClusterGraphNode] {
    def nodeEq(x: ClusterGraphNode, y: ClusterGraphNode): Boolean = x.host == y.host && x.port == y.port
  }
  implicit object MemberNodeLike extends NodeLike[ClusterMember] {
    def nodeEq(x: ClusterMember, y: ClusterMember): Boolean = x.address.host == y.address.host && x.address.port == y.address.port
  }
}

trait MemberLike[S, T] {
  def nodeEq(x: S, y: T): Boolean
}

object MemberLike {

  implicit object ClusterMemberLike extends MemberLike[ClusterGraphNode, ClusterMember] {
    def nodeEq(x: ClusterGraphNode, y: ClusterMember): Boolean = x.host == y.address.host && x.port == y.address.port
  }

}

