package clusterconsole

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.util.Timeout
import clusterconsole.core.LogF
import clusterconsole.http._
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object ClusterConsoleApp extends App with LogF {

  args.logDebug("ClusterConsoleApp starting with args:" + _.toList.toString)

  val akkaConf =
    """akka.remote.netty.tcp.hostname="127.0.0.1"
      |akka.remote.netty.tcp.port=2771
      |akka.cluster.roles = [clusterconsole]
      |""".stripMargin

  val config = ConfigFactory.parseString(akkaConf).withFallback(ConfigFactory.load())

  val clusterConsoleSystem = ActorSystem("ClusterConsoleSystem", config)

  val router: ActorRef = clusterConsoleSystem.actorOf(Props[RouterActor], "router")

  //clusterConsoleSystem.scheduler.schedule(3 seconds, 10 seconds, clusterAwareActor, ClusterMemberUp("cluster1", "name: " + System.currentTimeMillis()))

  clusterConsoleSystem.actorOf(HttpServiceActor.props("127.0.0.1", 8080, Timeout(30 seconds), router), "clusterconsolehttp")

}
