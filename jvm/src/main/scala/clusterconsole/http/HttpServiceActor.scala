package clusterconsole.http

import akka.http.scaladsl.server.RoutingSetup._
import akka.pattern.{ ask, pipe }
import akka.stream.scaladsl.ImplicitMaterializer

import akka.actor.{ Props, ActorRef, Actor }
import akka.actor.Actor.Receive
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.scaladsl.ImplicitMaterializer
import akka.util.Timeout
import clusterconsole.core.LogF

import scala.concurrent.Future
import scala.util.{ Failure, Success }

class HttpServiceActor(host: String,
  port: Int,
  selfTimeout: Timeout)
    extends Actor with ClusterConsoleRoutes with ImplicitMaterializer with LogF {

  import context.dispatcher

  def startHttpServer: Future[ServerBinding] = {
    // Boot the server
    Http(context.system).bindAndHandle(routes(materializer), host, port).pipeTo(self)
  }

  startHttpServer.onComplete {
    case Success(binding) =>
      binding.localAddress.toString.logInfo("\r\n\r\nHttpService started, ready to service requests on " + _)
    case Failure(ex) =>
      ex.printStackTrace()
      sys.exit(1)
  }

  def receive: Receive = {
    case _ =>
  }
}

object HttpServiceActor {
  def props(host: String,
    port: Int,
    selfTimeout: Timeout): Props =
    Props(new HttpServiceActor(host, port, selfTimeout))

}

