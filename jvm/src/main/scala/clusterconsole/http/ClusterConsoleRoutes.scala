package clusterconsole.http

import akka.actor.{ Actor, Props, ActorRef }
import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.{ PredefinedToEntityMarshallers, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes
import akka.stream.scaladsl.{ Source, Merge, FlowGraph, Flow }
import clusterconsole.core.LogF

import scala.util.Random

trait ClusterConsoleRoutes extends LogF {

  val router: ActorRef
  val clusterAwareActor: ActorRef

  implicit def marshaller: ToEntityMarshaller[String] =
    PredefinedToEntityMarshallers.stringMarshaller(MediaTypes.`text/html`)

  def routes(implicit mat: ActorMaterializer): Route = initialPageRoute

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
      } ~ path("api" / "events") {
        handleWebsocketMessages(clusterSocketFlow(router))
      } ~ path("api" / "clusters") {
        handleWebsocketMessages(clusterSocketFlow(router))
      }
    }


  def handleCommand: Flow[Message, Message, Unit] = {
    Flow[Message].map {
      case TextMessage.Strict(txt) =>

        import Json._


        println("-------------- "+txt)


        TextMessage.Strict(txt.reverse)
      case _ => TextMessage.Strict("Not supported message type")
    }
  }


  def clusterSocketFlow(router: ActorRef): Flow[Message, Message, Unit] = {
    Flow() { implicit builder =>
      import FlowGraph.Implicits._

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

  def randomPrintableString(length: Int, start: String = ""): String = {
    if (length == 0) start else randomPrintableString(length - 1, start + Random.nextPrintableChar())
  }

}

