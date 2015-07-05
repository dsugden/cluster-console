package clusterconsole

import akka.actor.ActorSystem
import akka.util.Timeout
import clusterconsole.core.LogF
import clusterconsole.http.HttpServiceActor
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

object ClusterConsoleApp extends App with LogF {

  args.logDebug("ClusterConsoleApp starting with args:" + _.toList.toString)

  val argumentsError = """
   Please run the service with the required arguments:  <hostIpAddress> <akka-port> <http-port>"""

  assert(args.length == 3, argumentsError)

  val hostname = args(0)
  val akkaPort = args(1).toInt
  val httpPort = args(2).toInt

  val akkaConf =
    """akka.remote.netty.tcp.hostname="%hostname%"
      |akka.remote.netty.tcp.port=%port%
      |akka.cluster.roles = [loginhttp]
      |""".stripMargin

  val config = ConfigFactory.parseString(akkaConf.replaceAll("%hostname%", hostname)
    .replaceAll("%port%", akkaPort.toString)).withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterConsoleSystem", config)

  system.actorOf(HttpServiceActor.props(hostname, httpPort, Timeout(30 seconds)), "http")

}
