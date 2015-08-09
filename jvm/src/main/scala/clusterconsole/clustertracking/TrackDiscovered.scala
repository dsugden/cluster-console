package clusterconsole.clustertracking

import akka.actor.{ ActorRef, Props, Actor }
import clusterconsole.core.LogF
import clusterconsole.http._

case object GetDiscovered

case class GetDiscovered(system: String)

case class UpdateDiscoveredDependencies(cluster: DiscoveredCluster)

object TrackDiscovered {
  def props(socketPublisherRouter: ActorRef): Props = Props(new TrackDiscovered(socketPublisherRouter))
}

class TrackDiscovered(socketPublisherRouter: ActorRef) extends Actor with LogF {

  def receive: Receive = track(Map.empty[String, DiscoveredCluster])

  def track(discoveredClustersMap: Map[String, DiscoveredCluster]): Actor.Receive = {
    case IsDiscovered(discoveredcluster) =>
      //add to map only if not there
      val cluster = discoveredClustersMap.get(discoveredcluster.system).fold(discoveredcluster)(identity)
      context.become(track(discoveredClustersMap + (discoveredcluster.system -> cluster)))

    case GetDiscovered =>
      sender() ! discoveredClustersMap.values.toSet

    case GetDiscovered(system) =>
      sender() ! discoveredClustersMap.get(system)

    case m: ClusterEvent =>
      m match {
        case ClusterMemberUp(system, member) =>
          val newdiscoveredClustersMap = discoveredClustersMap.get(system).map { newDc =>

            newDc.logDebug("_______________ ClusterMemberUp system  " + system + " member " + member + " " + _)
            discoveredClustersMap + (system -> newDc.copy(members =
              newDc.members.find(e => e.address == member.address).fold(newDc.members + member)(found =>
                newDc.members.map(m => if (m.address == member.address) { m.copy(state = Up) } else m))))
          }

          newdiscoveredClustersMap.logDebug("****************************  newdiscoveredClustersMap " + _)

          context.become(track(newdiscoveredClustersMap.getOrElse(discoveredClustersMap)))

        case ClusterMemberUnreachable(system, member) =>
          val newdiscoveredClustersMap = discoveredClustersMap.get(system).map { newDc =>
            discoveredClustersMap + (system -> newDc.copy(members = newDc.members.map(m =>
              if (m.address == member.address) {
                m.copy(state = Unreachable)
              } else {
                m
              }
            )))
          }
          context.become(track(newdiscoveredClustersMap.getOrElse(discoveredClustersMap)))

        case ClusterMemberRemoved(system, member) =>
          val newdiscoveredClustersMap = discoveredClustersMap.get(system).map { newDc =>

            newDc.logDebug("_______________ ClusterMemberRemoved  " + _)

            discoveredClustersMap + (system -> newDc.copy(members = newDc.members.map(m =>
              if (m.address == member.address) {
                m.copy(state = Removed)
              } else {
                m
              }
            )))
          }
          context.become(track(newdiscoveredClustersMap.getOrElse(discoveredClustersMap)))

        case _ =>

      }
      socketPublisherRouter.forward(m)

    case UpdateDiscoveredDependencies(cluster) =>
      val newdiscoveredClustersMap = discoveredClustersMap.get(cluster.system).map { newDc =>

        cluster.logDebug("_______________ UpdateDiscoveredDependencies  " + _)
        discoveredClustersMap + (cluster.system -> newDc.copy(dependencies = cluster.dependencies))
      }
      context.become(track(newdiscoveredClustersMap.getOrElse(discoveredClustersMap)))
      sender() ! true

  }
}
