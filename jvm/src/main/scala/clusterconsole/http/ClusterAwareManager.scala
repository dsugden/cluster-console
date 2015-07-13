package clusterconsole.http

import akka.actor.{ Address, Actor }
import akka.actor.Actor.Receive
import akka.cluster.Cluster

import scala.collection.immutable

case class HostPort(host: String, port: Int)

case class Discover(system: String, seedNodes: List[HostPort])

class ClusterAwareManager extends Actor {

  def receive: Receive = {

    case Discover(system, seedNodes) =>

      val addresses: immutable.Seq[Address] = seedNodes.map(e => Address("tcp", system, e.host, e.port))

      Cluster(context.system).joinSeedNodes(addresses)

  }
}
