package clusterconsole.http

import akka.actor.{ ActorLogging, Actor, ActorRef, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.RoutingSetup._
import akka.pattern.pipe
import akka.stream.scaladsl.ImplicitMaterializer
import akka.util.Timeout
import clusterconsole.core.LogF

import scala.concurrent.Future
import scala.util.{ Failure, Success }

class HttpServiceActor(host: String, port: Int, selfTimeout: Timeout, val socketPublisherRouter: ActorRef)
    extends Actor
    with ClusterConsoleRoutes
    with ImplicitMaterializer
    with ActorLogging {

  import context.dispatcher

  def startHttpServer: Future[ServerBinding] = {
    // Boot the server
    Http(context.system).bindAndHandle(routes(materializer), host, port).pipeTo(self)
  }

  startHttpServer.onComplete {
    case Success(binding) =>
      log.info("\r\n\r\nHttpService started, ready to service requests on {}", binding.localAddress)
    case Failure(ex) =>
      ex.printStackTrace()
      sys.exit(1)
  }

}

object HttpServiceActor {
  def props(host: String, port: Int, selfTimeout: Timeout, socketPublisherRouter: ActorRef): Props =
    Props(new HttpServiceActor(host, port, selfTimeout, socketPublisherRouter))
}

