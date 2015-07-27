package clusterconsole.clustertracking

import akka.actor.{ Props, Actor }
import clusterconsole.core.LogF
import clusterconsole.http.DiscoveredCluster

case object GetDiscovered

case class GetDiscovered(system: String)

object TrackDiscovered {
  def props: Props = Props(new TrackDiscovered)
}

class TrackDiscovered extends Actor with LogF {

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

  }
}
