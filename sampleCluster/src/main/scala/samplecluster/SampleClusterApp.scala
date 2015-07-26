package samplecluster

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object SampleClusterApp extends App {

  val akkaConf =
    """akka.remote.netty.tcp.hostname="%hostname%"
      |akka.remote.netty.tcp.port=%port%
      |akka.cluster.roles = [%role%]
      |""".stripMargin

  val argumentsError = """
   Please run the service with the required arguments:  <hostIpAddress> <port> <actor: BackEnd | FrontEnd  >"""

  assert(args.length == 3, argumentsError)

  val hostname = args(0)
  val port = args(1).toInt
  val role = args(2)

  val clusterConfig = ConfigFactory.parseString(akkaConf.replaceAll("%hostname%", hostname)
    .replaceAll("%port%", port.toString).replaceAll("%role%", role)).withFallback(ConfigFactory.load())

  val clusterSystem = ActorSystem("SampleClusterSystem", clusterConfig)

  role match {
    case "BackEnd" => clusterSystem.actorOf(BackEnd.props, "back-end-actor")
    case "FrontEnd" => clusterSystem.actorOf(FrontEnd.props, "front-end-actor")
    case _ =>
  }

}
