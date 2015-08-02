package samplecluster

import akka.actor.{ Actor, Props, ActorSystem }
import com.typesafe.config.ConfigFactory

object SampleClusterApp extends App {

  val akkaConf =
    """akka.remote.netty.tcp.hostname="%hostname%"
      |akka.remote.netty.tcp.port=%port%
      |akka.cluster.roles = [%role%]
      |akka.cluster {
      |    seed-nodes = [
      |      "akka.tcp://%clustername%@%clusterseed1%"]
      |    auto-down-unreachable-after = 10s
      |  }
      |
      |""".stripMargin

  val argumentsError = """
   Please run the service with the required arguments:  <hostIpAddress> <port> <actor: BackEnd | FrontEnd  >"""

  assert(args.length == 5, argumentsError)

  val hostname = args(0)
  val port = args(1).toInt
  val clusterName = args(2)
  val clusterSeed = args(3)
  val role = args(4)

  println("clusterName = " + clusterName)
  println("clusterSeed = " + clusterSeed)
  println("role = " + role)

  val clusterConfig = ConfigFactory.parseString(akkaConf.replaceAll("%hostname%", hostname)
    .replaceAll("%port%", port.toString).replaceAll("%role%", role)
    .replaceAll("%clustername%", clusterName)
    .replaceAll("%clusterseed1%", clusterSeed)).withFallback(ConfigFactory.load())

  val clusterSystem = ActorSystem(clusterName, clusterConfig)

  role match {
    case "Stable-Seed" => clusterSystem.actorOf(ClusterActor.props, "foo-http-actor")
    case "Foo-Http" => clusterSystem.actorOf(ClusterActor.props, "foo-http-actor")
    case "Foo-Worker" => clusterSystem.actorOf(ClusterActor.props, "foo-worker-actor")
    case "Bar-Http" => clusterSystem.actorOf(ClusterActor.props, "bar-http-actor")
    case "Bar-Worker" => clusterSystem.actorOf(ClusterActor.props, "bar-http-actor")
    case "Baz-Security" => clusterSystem.actorOf(ClusterActor.props, "baz-security-actor")
    case _ =>
  }

}

object ClusterActor {
  def props: Props = Props(new ClusterActor)
}

class ClusterActor extends Actor {
  def receive: Receive = {
    case _ =>
  }
}
