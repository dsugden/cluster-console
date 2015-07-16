package clusterconsole.http

import akka.actor.{ Actor, Address }
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{ MemberExited, MemberRemoved, MemberUp }

import scala.collection.immutable

class ClusterAwareManager extends Actor {

  val cluster = Cluster(context.system)

  /** subscribe to cluster event in order to track workers */
  override def preStart(): Unit =
    cluster.subscribe(self, classOf[MemberUp])

  cluster.subscribe(self, classOf[MemberExited])
  cluster.subscribe(self, classOf[MemberRemoved])

  override def postStop() = cluster.unsubscribe(self)

  def receive: Receive = {

    case Discover(system, seedNodes) =>

      println("%%%%%%%%%%%%%%    ClusterAwareManager Discover")

      val addresses: immutable.Seq[Address] = seedNodes.map(e => Address("akka.tcp", system, e.host, e.port))

      Cluster(context.system).joinSeedNodes(addresses)

  }
}
