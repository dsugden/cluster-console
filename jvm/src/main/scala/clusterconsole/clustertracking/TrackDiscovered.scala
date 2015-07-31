package clusterconsole.clustertracking

import akka.actor.{ ActorRef, Props, Actor }
import clusterconsole.core.LogF
import clusterconsole.http._

case object GetDiscovered

case class GetDiscovered(system: String)

object TrackDiscovered {
  def props(socketPublisherRouter: ActorRef): Props = Props(new TrackDiscovered(socketPublisherRouter))
}

class TrackDiscovered(socketPublisherRouter: ActorRef) extends Actor with LogF {

  def receive: Receive = track(Map.empty[String, DiscoveredCluster])

  def track(discoveredClustersMap: Map[String, DiscoveredCluster]): Actor.Receive = {
    case IsDiscovered(discoveredcluster) =>
      discoveredClustersMap.logDebug("*************  TrackDiscovered IsDiscovered system " + _)
      context.become(track(discoveredClustersMap + (discoveredcluster.system -> discoveredcluster)))

    case GetDiscovered =>
      discoveredClustersMap.logDebug("*************  TrackDiscovered GetDiscovered discovered " + _)
      sender() ! discoveredClustersMap.values.toSet

    case GetDiscovered(system) =>
      discoveredClustersMap.logDebug("*************  TrackDiscovered GetDiscovered discovered " + _)
      sender() ! discoveredClustersMap.get(system)

    case m: ClusterEvent =>
      m match {
        case ClusterMemberUp(system, member) =>
          val newdiscoveredClustersMap = discoveredClustersMap.get(system).map { newDc =>
            discoveredClustersMap + (system -> newDc.copy(members = newDc.members + member))
          }
          context.become(track(newdiscoveredClustersMap.logDebug("*********** ClusterMemberUp newdiscoveredClustersMap= " + _).getOrElse(discoveredClustersMap)))

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
          context.become(track(newdiscoveredClustersMap.logDebug("*********** ClusterMemberUp newdiscoveredClustersMap= " + _).getOrElse(discoveredClustersMap)))

        case ClusterMemberRemoved(system, member) =>
          val newdiscoveredClustersMap = discoveredClustersMap.get(system).map { newDc =>
            discoveredClustersMap + (system -> newDc.copy(members = newDc.members.map(m =>
              if (m.address == member.address) {
                m.copy(state = Removed)
              } else {
                m
              }
            )))
          }
          context.become(track(newdiscoveredClustersMap.logDebug("*********** ClusterMemberRemoved  newdiscoveredClustersMap= " + _).getOrElse(discoveredClustersMap)))

        case _ =>

      }
      socketPublisherRouter.forward(m)

  }
}
