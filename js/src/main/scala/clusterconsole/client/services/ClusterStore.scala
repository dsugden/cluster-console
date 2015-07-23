package clusterconsole.client.services

import autowire._
import clusterconsole.client.services.Logger._
import clusterconsole.client.ukko.Actor
import clusterconsole.http._
import rx._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

case object RefreshClusterMembers

case class UpdateClusterForm(clusterForm: ClusterForm)

trait ClusterStore extends Actor {

  // refine a reactive variable
  private val items = Var(Map.empty[String, DiscoveredCluster])

  private val events = Var(Seq.empty[ClusterEvent])

  private val clusterForm = Var(ClusterForm.initial)

  def clusterMembers: Rx[Map[String, DiscoveredCluster]] = items

  def clusterEvents: Rx[Seq[ClusterEvent]] = events

  def getClusterForm: Rx[ClusterForm] = clusterForm

  def name: String = "ClusterStore"

  /**
   * Actors need to override this function to define their behavior
   *
   * @return `PartialFunction` defining actor behavior
   */
  def receive: ClusterStore.Receive = {
    case clusterMemberUp: ClusterMemberUp =>
      log.debug("+++++++++++ receive clusterMemberUp" + clusterMemberUp)
      events() = events() :+ clusterMemberUp

    case DiscoveryBegun(name, seeds) =>
      log.debug("+++++++++++ receive " + DiscoveryBegun(name, seeds))
      items() = items() + (name -> DiscoveredCluster(name, seeds, "Discovery begun"))

    case clusterUnjoin: ClusterUnjoin =>
      log.debug("+++++++++++ receive ClusterUnjoin" + clusterUnjoin)
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

  def subscribeToCluster(actor: Actor, name: String, seedNodes: List[HostPort]) = {
    AjaxClient[Api].discover(name, seedNodes).call().foreach { discoveryBegun =>
      MainDispatcher.dispatch(discoveryBegun)
    }
  }

  def updateClusterForm(clusterForm: ClusterForm) = {
    MainDispatcher.dispatch(UpdateClusterForm(clusterForm))
  }

  //AjaxClient[Api].discover("SampleClusterSystem", List(HostPort("127.0.0.1",2551))).call().foreach( s =>
}
