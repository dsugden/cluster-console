package clusterconsole.http


trait Api {

  def discover(system:String, selfHost:String, seedNodes:List[HostPort]): Option[DiscoveryBegun]

  def getDiscovering(): Seq[DiscoveryBegun]

  def getDiscovered(): Set[DiscoveredCluster]

  def getCluster(system:String): Option[DiscoveredCluster]

  def updateClusterDependencies(cluster:DiscoveredCluster):DiscoveredCluster

}
