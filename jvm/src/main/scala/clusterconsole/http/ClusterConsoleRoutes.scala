package clusterconsole.http

import akka.actor._
import akka.http.scaladsl.marshalling.{ PredefinedToEntityMarshallers, ToEntityMarshaller }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, MediaTypes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ RequestContext, Route }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, FlowGraph, Merge, Source }
import clusterconsole.core.LogF

import scala.collection.mutable.{ Map => MutableMap }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait ClusterConsoleRoutes extends LogF { this: Actor =>

  def socketPublisherRouter: ActorRef

  lazy val clusterDiscoveryService = new ClusterDiscoveryService(context, socketPublisherRouter)

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
        handleWebsocketMessages(clusterSocketFlow(socketPublisherRouter))
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

  override def receive: Receive = {
    case Terminated(ref) =>
      logger.logDebug(s"Died: $ref" + _)
      //TODO: refactor
      clusterDiscoveryService.systems.get(ref).foreach(d => socketPublisherRouter ! ClusterUnjoin(d.systemName, d.seedNodes))
      clusterDiscoveryService.systems -= ref
  }

}

class ClusterDiscoveryService(context: ActorContext, socketPublisherRouter: ActorRef) extends Api with LogF {

  case class SystemDetails(systemName: String, seedNodes: List[HostPort])

  val systems: MutableMap[ActorRef, SystemDetails] = MutableMap.empty

  def discover(systemName: String, seedNodes: List[HostPort]): DiscoveryBegun = {

    val newSystemActor = context.system.actorOf(ClusterAware.props(systemName, seedNodes, socketPublisherRouter))

    systems += newSystemActor -> SystemDetails(systemName, seedNodes)

    context.watch(newSystemActor)

    //newSystemActor ! Discover(system, seedNodes)

    "discover begun".logDebug("************   " + _)
    DiscoveryBegun(systemName, seedNodes)

  }

}

object AutowireServer extends autowire.Server[String, upickle.Reader, upickle.Writer] {
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

