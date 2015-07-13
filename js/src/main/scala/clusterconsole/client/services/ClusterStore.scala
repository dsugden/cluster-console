package clusterconsole.client.services

import clusterconsole.client.ukko.Actor
import clusterconsole.http.{ClusterMemberUp, DiscoveredCluster, ClusterMember}
import org.scalajs.dom.raw.WebSocket
import rx._
import clusterconsole.client.services.Logger._

case object RefreshClusterMembers


trait ClusterStore extends Actor{


  WebSocketClient.subscribe(this)


  // refine a reactive variable
  private val items = Var(Map.empty[String,DiscoveredCluster])

  private val events = Var(Seq.empty[ClusterMemberUp])


  def clusterMembers:Rx[Map[String,DiscoveredCluster]] = items

  def clusterEvents:Rx[Seq[ClusterMemberUp]] = events


  def name: String = "ClusterStore"

  /**
   * Actors need to override this function to define their behavior
   *
   * @return `PartialFunction` defining actor behavior
   */
  def receive: ClusterStore.Receive = {
    case clusterMemberUp: ClusterMemberUp =>
      log.debug("+++++++++++ receive " + clusterMemberUp)
      events() = events() :+ clusterMemberUp


    case other => log.debug("other " + other)

  }


}

object ClusterStore extends ClusterStore{
  // register this actor with the dispatcher
  MainDispatcher.register(this)

}
