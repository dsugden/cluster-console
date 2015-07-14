package clusterconsole.http


trait Api {

  def discover(system:String, seedNodes:List[HostPort]):String

}
