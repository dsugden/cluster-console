package clusterconsole.http

import java.nio.ByteBuffer

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import akka.http.scaladsl.server.{ RequestContext, Route }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.{ PredefinedToEntityMarshallers, ToEntityMarshaller }
import akka.http.scaladsl.model.{ HttpEntity, ContentTypes, MediaTypes }
import akka.stream.scaladsl.{ Source, Merge, FlowGraph, Flow }
import akka.util.ByteString
import clusterconsole.core.LogF
import com.google.common.net.HostAndPort
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.Random

trait ClusterConsoleRoutes extends LogF { this: Actor =>

  val router: ActorRef
  val clusterAwareActor: ActorRef

  lazy val clusterDiscoveryService = new ClusterDiscoveryService(context.system)

  implicit def marshaller: ToEntityMarshaller[String] =
    PredefinedToEntityMarshallers.stringMarshaller(MediaTypes.`text/html`)

  def routes(implicit mat: ActorMaterializer): Route = initialPageRoute

  def httpRequest2String(req: RequestContext)(implicit mat: ActorMaterializer): Future[String] =
    req.request.entity.withContentType(ContentTypes.`application/json`).toStrict(1 second).map(b => new String(b.data.toArray))

  def initialPageRoute(implicit mat: ActorMaterializer): Route =
    get {
      pathSingleSlash {
        complete(
          Page.main("Cluster Console").toString()
        )
      } ~ pathPrefix("srcmaps") {
        getFromDirectory("../")
      } ~ {
        getFromResourceDirectory("web")
      } ~ path("api") {

        complete("")

      } ~ path("events") {
        handleWebsocketMessages(clusterSocketFlow(router))
      }
    } ~ post {
      path("api" / Segments) { segments =>
        extract(httpRequest2String) { dataExtractor =>
          complete {
            for {
              data <- dataExtractor
              response <- AutowireServer.route[Api](clusterDiscoveryService)(autowire.Core.Request(
                segments, upickle.read[Map[String, String]](data.logDebug("***********  data: " + _)))
              )
            } yield HttpEntity(response)
          }
        }
      }
    }

  def clusterSocketFlow(router: ActorRef): Flow[Message, Message, Unit] = {
    Flow() { implicit builder =>
      import FlowGraph.Implicits._

      "clusterSocketFlow".logDebug("here in " + _)

      // create an actor source
      val source = Source.actorPublisher[String](Props(classOf[ClusterPublisher], router))
      val filter = builder.add(Flow[String].filter(_ => false))
      val merge = builder.add(Merge[String](2))

      val mapMsgToString = builder.add(Flow[Message].map[String] { msg => "" })
      val mapStringToMsg = builder.add(Flow[String].map[Message](x => TextMessage.Strict(x)))

      val clusterEventsSource = builder.add(source)

      // connect the graph
      //      mapMsgToResponse ~> filter ~> merge
      mapMsgToString ~> filter ~> merge
      clusterEventsSource ~> merge ~> mapStringToMsg

      // expose ports
      (mapMsgToString.inlet, mapStringToMsg.outlet)

    }
  }

}

class ClusterDiscoveryService(context: ActorSystem) extends Api with LogF {

  var systems: List[ActorRef] = List()

  def discover(system: String, seedNodes: List[HostPort]) = {

    val newSystemActor = context.actorOf(ActorSystemManager.props(system))

    newSystemActor ! Discover(system, seedNodes)

    "discovered".logDebug("************   " + _)

  }

}

object AutowireServer extends autowire.Server[String, upickle.Reader, upickle.Writer] {
  import Json._
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

