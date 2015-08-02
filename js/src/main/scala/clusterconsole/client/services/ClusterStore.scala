package clusterconsole.client.services

import autowire._
import clusterconsole.client.d3.Layout.GraphNode
import clusterconsole.client.services.Logger._
import clusterconsole.client.ukko.Actor
import clusterconsole.http._
import rx._
import upickle.default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

case object RefreshClusterMembers

case class UpdateClusterForm(clusterForm: ClusterForm)

case class SelectedCluster(c: DiscoveredCluster)

case class RefreshCluster(c: DiscoveredCluster)

case class UpdateClusterNodes(system: String, node: List[GraphNode])

trait ClusterStore extends Actor {

  // refine a reactive variable
  private val discoveredClusters = Var(Map.empty[String, DiscoveredCluster])

  private val discoveredClusterNodes = Var(Map.empty[String, List[GraphNode]])

  private val selectedCluster = Var(Option.empty[DiscoveredCluster])

  private val discoveringClusters = Var(Map.empty[String, DiscoveryBegun])

  private val events = Var(Seq.empty[ClusterEvent])

  private val clusterForm = Var(ClusterForm.initial)

  def getDiscoveredClusters: Rx[Map[String, DiscoveredCluster]] = discoveredClusters

  def getDiscoveredClusterNodes: Rx[Map[String, List[GraphNode]]] = discoveredClusterNodes

  def getDiscoveringClusters: Rx[Map[String, DiscoveryBegun]] = discoveringClusters

  def getSelectedCluster: Rx[Option[DiscoveredCluster]] = selectedCluster

  //  def clusterEvents: Rx[Seq[ClusterEvent]] = events

  def getClusterForm: Rx[ClusterForm] = clusterForm

  def name: String = "ClusterStore"

  /**
   * Actors need to override this function to define their behavior
   *
   * @return `PartialFunction` defining actor behavior
   */
  def receive: ClusterStore.Receive = {
    case m @ ClusterMemberUp(system, clusterMember) =>
      //      log.debug("+++++++++++ receive clusterMemberUp" + m + " system:" + system)
      ClusterStoreActions.refreshCluster(system)

    case m @ ClusterMemberUnreachable(system, clusterMember) =>
      //      log.debug("+++++++++++ receive clusterMemberUp" + m + " system:" + system)
      ClusterStoreActions.refreshCluster(system)

    case m @ ClusterMemberRemoved(system, clusterMember) =>
      //      log.debug("+++++++++++ receive clusterMemberUp" + m + " system:" + system)
      ClusterStoreActions.refreshCluster(system)

    case m @ DiscoveryBegun(system, seedNodes) =>
      log.debug("+++++++++++ receive DiscoveryBegun" + m)
      discoveringClusters() = discoveringClusters() + (system -> m)

    case m @ DiscoveredCluster(system, seeds, status, members) =>
      //      log.debug("+++++++++++ receive DiscoveredCluster" + m)
      discoveringClusters() = discoveringClusters() - system
      discoveredClusters() = discoveredClusters() + (system -> m)

    case m @ SelectedCluster(DiscoveredCluster(system, seeds, status, members)) =>
      selectedCluster() = Some(m.c)

    case m @ RefreshCluster(DiscoveredCluster(system, seeds, status, members)) =>
      discoveringClusters() = discoveringClusters() - system
      discoveredClusters() = discoveredClusters() + (system -> m.c)
      if (selectedCluster().isEmpty)
        selectedCluster() = Some(m.c)

    case m @ UpdateClusterNodes(system, nodes) =>
      discoveredClusterNodes() = discoveredClusterNodes() + (system -> nodes)

    case clusterUnjoin: ClusterUnjoin =>
      //      log.debug("+++++++++++ receive ClusterUnjoin" + clusterUnjoin)
      events() = events() :+ clusterUnjoin

    case x: UpdateClusterForm =>
      clusterForm() = x.clusterForm

    case other => log.debug("other " + other)

  }

}

object ClusterStore extends ClusterStore {
  // register this actor with the dispatcher
  MainDispatcher.register(this)
}

object ClusterStoreActions {

  import Json._

  def subscribeToCluster(name: String, seedNodes: List[HostPort]) =
    AjaxClient[Api].discover(name, seedNodes).call()
      .foreach(_.foreach(MainDispatcher.dispatch))

  def getDiscoveringClusters() = {

    log.debug("clalling getDiscoveringClusters")

    AjaxClient[Api].getDiscovering().call().foreach { discoveringSeq =>
      discoveringSeq.foreach(MainDispatcher.dispatch)
    }
  }

  def getDiscoveredClusters() = {
    AjaxClient[Api].getDiscovered().call().foreach { discoveredSet =>
      discoveredSet.foreach(MainDispatcher.dispatch)
    }
  }

  def selectCluster(system: String) = {
    AjaxClient[Api].getCluster(system).call().foreach { discovered =>
      discovered.foreach(c => MainDispatcher.dispatch(SelectedCluster(c)))
    }
  }

  def refreshCluster(system: String) = {
    AjaxClient[Api].getCluster(system).call().foreach { discovered =>
      discovered.foreach(c => MainDispatcher.dispatch(RefreshCluster(c)))
    }
  }

  def updateClusterForm(clusterForm: ClusterForm) = {
    MainDispatcher.dispatch(UpdateClusterForm(clusterForm))
  }

  def updateClusterNode(system: String, nodes: List[GraphNode]) = {
    MainDispatcher.dispatch(UpdateClusterNodes(system, nodes))
  }
}
