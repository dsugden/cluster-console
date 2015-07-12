package clusterconsole.http

import akka.actor.{ Actor, Props, ActorRef }
import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import akka.routing.{ RemoveRoutee, AddRoutee, Routee }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{ MediaTypes, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.{ PredefinedToEntityMarshallers, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes
import akka.stream.scaladsl.{ Source, Merge, FlowGraph, Flow }
import akka.stream.stage.{ TerminationDirective, SyncDirective, Context, PushStage }
import clusterconsole.core.LogF
import clusterconsole.http.{ TestResponse, TestMessage, ClusterEvent }

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
      } ~ path("api") {
        handleWebsocketMessages(clusterSocketFlow(router))
      }
    }

  def graphFlowWithExtraSource: Flow[Message, Message, Unit] = {
    Flow() { implicit b =>
      import FlowGraph.Implicits._

      // Graph elements we'll use
      val merge = b.add(Merge[Int](2))
      val filter = b.add(Flow[Int].filter(_ => false))

      // convert to int so we can connect to merge
      val mapMsgToInt = b.add(Flow[Message].map[Int] { msg => -1 })
      val mapIntToMsg = b.add(Flow[Int].map[Message](x => TextMessage.Strict(":" + randomPrintableString(200) + ":" + x.toString)))
      val log = b.add(Flow[Int].map[Int](x => {
        println(x); x
      }))

      // source we want to use to send message to the connected websocket sink
      val rangeSource = b.add(Source(1 to 2000))

      // connect the graph
      mapMsgToInt ~> filter ~> merge // this part of the merge will never provide msgs
      rangeSource ~> log ~> merge ~> mapIntToMsg

      // expose ports
      (mapMsgToInt.inlet, mapIntToMsg.outlet)
    }
  }

  def clusterSocketFlow(router: ActorRef): Flow[Message, Message, Unit] = {
    Flow() { implicit builder =>
      import FlowGraph.Implicits._

      // create an actor source
      val source = Source.actorPublisher[String](Props(classOf[ClusterPublisher], router))

      val merge = builder.add(Merge[String](2))

      val mapMsgToResponse = builder.add(Flow[Message].map[String] {

        case TextMessage.Strict(text) =>
          text.logDebug("+++++++++++++++++++++++++++  clusterSocketFlow mapMsgToResponse =   " + _)
          val msg = upickle.read[TestMessage](text)
          upickle.write(TestResponse(msg.v + "  lol"))
      })
      val mapStringToMsg = builder.add(Flow[String].map[Message](x => TextMessage.Strict(x)))

      val statsSource = builder.add(source)

      // connect the graph
      //      mapMsgToResponse ~> filter ~> merge
      mapMsgToResponse ~> merge
      statsSource ~> merge ~> mapStringToMsg

      // expose ports
      (mapMsgToResponse.inlet, mapStringToMsg.outlet)

    }
  }

  def randomPrintableString(length: Int, start: String = ""): String = {
    if (length == 0) start else randomPrintableString(length - 1, start + Random.nextPrintableChar())
  }

}

