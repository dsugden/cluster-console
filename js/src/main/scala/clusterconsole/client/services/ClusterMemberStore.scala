package clusterconsole.client.services

import clusterconsole.client.ukko.Actor
import clusterconsole.shared.ClusterMember
import rx._

case object RefreshClusterMembers


trait ClusterMemberStore extends Actor{



  // refine a reactive variable
  private val items = Var(Seq.empty[ClusterMember])

  def clusterMembers:Rx[Seq[ClusterMember]] = items


  def name: String = "ClusterMemberStore"

  /**
   * Actors need to override this function to define their behavior
   *
   * @return `PartialFunction` defining actor behavior
   */
  def receive: ClusterMemberStore.Receive = {
    case _ =>
  }


}

object ClusterMemberStore extends ClusterMemberStore{
  // register this actor with the dispatcher
  MainDispatcher.register(this)

}
