package clusterconsole.http

sealed trait ClusterDependency{
  val name: String
  val typeName: String
  def updateName(n:String) = this match {
    case m:DistributedRouter => m.copy(name = n)
    case m:ClusterSharded => m.copy(name = n)
    case m:Manual => m.copy(name = n)
    case m:Singleton => m.copy(name = n)
  }
}

case class DistributedRouter(name:String) extends ClusterDependency{
  val typeName = "Distributed Router"
}

case class ClusterSharded(name:String) extends ClusterDependency{
  val typeName = "Cluster Sharded"
}

case class Manual(name:String) extends ClusterDependency{
  val typeName = "Manual"
}

case class Singleton(name:String) extends ClusterDependency{
  val typeName = "Singleton"
}

