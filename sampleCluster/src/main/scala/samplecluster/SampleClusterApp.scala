package samplecluster

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object SampleClusterApp extends App {

  val akkaConf =
    """akka.remote.netty.tcp.hostname="%hostname%"
      |akka.remote.netty.tcp.port=%port%
      |akka.cluster.roles = [backend]
      |""".stripMargin

  val argumentsError = """
   Please run the service with the required arguments:  <hostIpAddress> <port> """

  assert(args.length == 2, argumentsError)

  val hostname = args(0)
  val port = args(1).toInt

  val clusterConfig = ConfigFactory.parseString(akkaConf.replaceAll("%hostname%", hostname)
    .replaceAll("%port%", port.toString)).withFallback(ConfigFactory.load())

  val clusterSystem = ActorSystem("SampleClusterSystem", clusterConfig)
  clusterSystem.actorOf(BackEnd.props, "back-end-actor")

}
