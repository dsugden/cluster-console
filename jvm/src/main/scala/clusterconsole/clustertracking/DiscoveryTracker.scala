package clusterconsole.clustertracking

import akka.actor.{ ActorLogging, ActorRef, Props, Actor }
import clusterconsole.http._

object DiscoveryTracker {
  case object GetDiscovered

  case class GetDiscovered(system: String)

  case class UpdateDiscoveredDependencies(cluster: DiscoveredCluster)

  def props(socketPublisher: ActorRef): Props = Props(new DiscoveryTracker(socketPublisher))
}

class DiscoveryTracker(socketPublisher: ActorRef) extends Actor with ActorLogging {

  import DiscoveryTracker._

  def receive: Receive =
    track(
      Map.empty[String, DiscoveredCluster]
    )

  def track(discoveredClusters: Map[String, DiscoveredCluster]): Actor.Receive = {
    case IsDiscovered(discoveredCluster) =>
      //add to map only if not there
      val cluster = discoveredClusters.
        get(discoveredCluster.system).
        fold(discoveredCluster)(identity)
      context.become(
        track(
          discoveredClusters + (discoveredCluster.system -> cluster)
        )
      )

    case GetDiscovered =>
      sender() ! discoveredClusters.values.toSet

    case GetDiscovered(system) =>
      sender() ! discoveredClusters.get(system)

    case m: ClusterEvent =>
      m match {
        case ClusterMemberUp(system, member) =>
          val newDiscoveredClusters = discoveredClusters.get(system).map { newMember =>

            log.debug("+++ [ClusterMemberUp] system: {} / member: {}", system, newMember)

            discoveredClusters + (system -> newMember.copy(members =
              newMember.members.
                find(e => e.address == member.address).
                fold(newMember.members + member)(found =>
                  newMember.members.
                    map(m =>
                      if (m.address == member.address) m.copy(state = Up) else m
                    )
                )
            )
            )
          }
          log.debug("*** [Member Up] New Discovered Clusters Members: {}", newDiscoveredClusters)

          context.become(
            track(
              newDiscoveredClusters.getOrElse(discoveredClusters)
            )
          )

        case ClusterMemberUnreachable(system, member) =>
          val newDiscoveredClusters = discoveredClusters.get(system).map { unreachableMember =>

            log.info("+++ [ClusterMemberUnreachable] system: {} / member: {}", system, unreachableMember)

            discoveredClusters + (system -> unreachableMember.copy(members =
              unreachableMember.members.
                map(m => if (m.address == member.address) m.copy(state = Unreachable) else m)
            ))
          }

          log.debug("*** [Member Unreachable] New Discovered Clusters Members: {}", newDiscoveredClusters)

          context.become(
            track(
              newDiscoveredClusters.getOrElse(discoveredClusters)
            )
          )

        case ClusterMemberRemoved(system, member) =>
          val newDiscoveredClusters = discoveredClusters.get(system).map { removedMember =>

            log.debug("+++ [ClusterMemberRemoved] system: {} / member: {}", system, removedMember)

            discoveredClusters + (system -> removedMember.copy(members =
              removedMember.members.map(m =>
                if (m.address == member.address) m.copy(state = Removed) else m
              )
            ))
          }

          log.debug("*** [Member Removed] New Discovered Clusters Members: {}", newDiscoveredClusters)

          context.become(
            track(
              newDiscoveredClusters.getOrElse(discoveredClusters)
            )
          )

        case _ =>

      }
      socketPublisher.forward(m)

    case UpdateDiscoveredDependencies(cluster) =>
      val newDiscoveredClusters = discoveredClusters.get(cluster.system).map { newDependency =>

        log.debug("+++ [DiscoveredDependencies] system: {} / member: {}", cluster.system, newDependency)

        discoveredClusters + (cluster.system -> newDependency.copy(dependencies = cluster.dependencies))
      }
      context.become(
        track(
          newDiscoveredClusters.getOrElse(discoveredClusters)
        )
      )

      sender() ! true
  }
}
