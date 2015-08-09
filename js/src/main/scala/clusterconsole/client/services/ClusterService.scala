package clusterconsole.client.services

import autowire._
import clusterconsole.client.components.ClusterGraphNode
import clusterconsole.client.services.Logger._
import clusterconsole.client.ukko.Actor
import clusterconsole.http._
import rx._
import upickle.default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

case object RefreshClusterMembers

case class UpdateClusterForm(clusterForm: ClusterForm)

case class SelectRoleDependency(system: String, rd: RoleDependency, selected: Boolean)

case class SelectedCluster(c: DiscoveredCluster)

case class RefreshCluster(c: DiscoveredCluster)

case class UpdatedCluster(c: DiscoveredCluster)

case class UpdateClusterNodes(system: String, node: ClusterGraphNode)

trait ClusterService extends Actor {

  // refine a reactive variable
  private val discoveredClusters = Var(Map.empty[String, DiscoveredCluster])

  private val discoveredClusterNodes = Var(Map.empty[String, List[ClusterGraphNode]])

  private val selectedDeps = Var(Map.empty[String, List[RoleDependency]])

  private val selectedCluster = Var(Option.empty[DiscoveredCluster])

  private val discoveringClusters = Var(Map.empty[String, DiscoveryBegun])

  private val events = Var(Seq.empty[ClusterEvent])

  private val clusterForm = Var(ClusterForm.initial)

  def getDiscoveredClusters: Rx[Map[String, DiscoveredCluster]] = discoveredClusters

  def getDiscoveredClusterNodes: Rx[Map[String, List[ClusterGraphNode]]] = discoveredClusterNodes

  def getDiscoveringClusters: Rx[Map[String, DiscoveryBegun]] = discoveringClusters

  def getSelectedCluster: Rx[Option[DiscoveredCluster]] = selectedCluster

  def getSelectedDeps: Rx[Map[String, List[RoleDependency]]] = selectedDeps

  //  def clusterEvents: Rx[Seq[ClusterEvent]] = events

  def getClusterForm: Rx[ClusterForm] = clusterForm

  def name: String = "ClusterStore"

  /**
   * Actors need to override this function to define their behavior
   *
   * @return `PartialFunction` defining actor behavior
   */
  def receive: ClusterService.Receive = {
    case m @ ClusterMemberUp(system, clusterMember) =>
      ClusterService.refreshCluster(system)

    case m @ ClusterMemberUnreachable(system, clusterMember) =>
      ClusterService.refreshCluster(system)

    case m @ ClusterMemberRemoved(system, clusterMember) =>
      ClusterService.refreshCluster(system)

    case m @ DiscoveryBegun(system, seedNodes) =>
      discoveringClusters() = discoveringClusters() + (system -> m)

    case m @ DiscoveredCluster(system, seeds, status, members, _) =>
      discoveringClusters() = discoveringClusters() - system
      discoveredClusters() = discoveredClusters() + (system -> m)

    case m @ SelectedCluster(DiscoveredCluster(system, seeds, status, members, _)) =>
      selectedCluster() = Some(m.c)

    case m @ RefreshCluster(DiscoveredCluster(system, seeds, status, members, deps)) =>
      discoveringClusters() = discoveringClusters() - system
      discoveredClusters() = discoveredClusters() + (system -> m.c)
      if (selectedCluster().isEmpty)
        selectedCluster() = Some(m.c)
      else {
        selectedCluster().foreach(c =>
          if (c.system == m.c.system)
            selectedCluster() = Some(m.c)
        )
      }

    case SelectRoleDependency(system, roleDependency, selected) =>
      if (selected) {
        selectedDeps() =
          selectedDeps() + (system -> selectedDeps().get(system).fold(List(roleDependency))(deps =>
            (deps.toSet + roleDependency).toList))
      } else {
        selectedDeps() =
          selectedDeps() + (system -> selectedDeps().get(system).fold(List.empty[RoleDependency])(deps =>
            (deps.toSet - roleDependency).toList))
      }

    case m @ UpdatedCluster(DiscoveredCluster(system, seeds, status, members, _)) =>
      selectedCluster().foreach(c =>
        if (c.system == m.c.system)
          selectedCluster() = Some(m.c)
      )

    case m @ UpdateClusterNodes(system, node) =>
      discoveredClusterNodes() =
        discoveredClusterNodes() + (system ->
          discoveredClusterNodes().get(system).fold(List(node))(nodes =>
            nodes.find(e => ClusterGraphNode.label(e) == ClusterGraphNode.label(node)).fold(node :: nodes)(found =>
              nodes.map(n =>
                if (ClusterGraphNode.label(n) == ClusterGraphNode.label(node)) {
                  node
                } else n)
            )
          ))

    case clusterUnjoin: ClusterUnjoin =>
      events() = events() :+ clusterUnjoin

    case x: UpdateClusterForm =>
      clusterForm() = x.clusterForm

    case other => log.debug("other " + other)

  }

}

object ClusterService extends ClusterService {
  MainDispatcher.register(this)

  import Json._

  def subscribeToCluster(name: String, seedNodes: List[HostPort]) =
    AjaxClient[Api].discover(name, seedNodes).call()
      .foreach(_.foreach(MainDispatcher.dispatch))

  def findDiscoveringClusters() =
    AjaxClient[Api].getDiscovering().call().foreach { discoveringSeq =>
      discoveringSeq.foreach(MainDispatcher.dispatch)
    }

  def findDiscoveredClusters() =
    AjaxClient[Api].getDiscovered().call().foreach { discoveredSet =>
      discoveredSet.foreach(MainDispatcher.dispatch)
    }

  def selectCluster(system: String) =
    AjaxClient[Api].getCluster(system).call().foreach { discovered =>
      discovered.foreach(c => MainDispatcher.dispatch(SelectedCluster(c)))
    }

  def refreshCluster(system: String) =
    AjaxClient[Api].getCluster(system).call().foreach { discovered =>
      discovered.foreach(c => MainDispatcher.dispatch(RefreshCluster(c)))
    }

  def updateClusterDependencies(cluster: DiscoveredCluster) =
    AjaxClient[Api].updateClusterDependencies(cluster).call().foreach { updated =>
      MainDispatcher.dispatch(UpdatedCluster(updated))
    }

  def selectRoleDependency(system: String, rd: RoleDependency, selected: Boolean) =
    MainDispatcher.dispatch(SelectRoleDependency(system, rd, selected))

  def updateClusterForm(clusterForm: ClusterForm) =
    MainDispatcher.dispatch(UpdateClusterForm(clusterForm))

  def updateClusterNode(system: String, node: ClusterGraphNode) =
    MainDispatcher.dispatch(UpdateClusterNodes(system, node))

}

