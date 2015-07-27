package clusterconsole.http


trait Api {

  def discover(system:String, seedNodes:List[HostPort]): Option[DiscoveryBegun]

  def getDiscovering(): Seq[DiscoveryBegun]

  def getDiscovered(): Set[DiscoveredCluster]

  def getCluster(system:String): Option[DiscoveredCluster]

}
