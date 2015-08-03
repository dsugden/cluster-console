package clusterconsole.http

sealed trait ClusterDependency{
  val name:String
  def updateName(n:String) = this match {
    case m:DistributedRouter => m.copy(name = n)
    case m:ClusterSharded => m.copy(name = n)
    case m:Manual => m.copy(name = n)
  }
}

case class DistributedRouter(name:String) extends ClusterDependency

case class ClusterSharded(name:String) extends ClusterDependency

case class Manual(name:String) extends ClusterDependency
