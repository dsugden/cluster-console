package clusterconsole.http

import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{ MediaTypes, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.{ PredefinedToEntityMarshallers, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes
import akka.stream.scaladsl.{ Source, Merge, FlowGraph, Flow }
import akka.stream.stage.{ TerminationDirective, SyncDirective, Context, PushStage }
import clusterconsole.core.LogF
import clusterconsole.shared.Protocol.TestMessage

import scala.util.Random

trait ClusterConsoleRoutes extends LogF {

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
        handleWebsocketMessages(graphFlowWithExtraSource)
      }
    }

  def websocketChatFlow: Flow[Message, Message, Unit] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) => msg.logDebug("*****************   websocketChatFlow msg: " + _) // unpack incoming WS text messages...
        // This will lose (ignore) messages not received in one chunk (which is
        // unlikely because chat messages are small) but absolutely possible
        // FIXME: We need to handle TextMessage.Streamed as well.

      }
      .map {
        case v =>

          v.logDebug("-----------  sending " + _)
          TextMessage.Strict(s"$v") // ... pack outgoing messages into WS text messages ...
      }
  //      .via(reportErrorsFlow) // ... then log any processing errors on stdin

  //  def reportErrorsFlow[T]: Flow[T, T, Unit] =
  //    Flow[T]
  //      .transform(() ⇒ new PushStage[T, T] {
  //      def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)
  //
  //      override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
  //        println(s"WS stream failed with $cause")
  //        super.onUpstreamFailure(cause, ctx)
  //      }
  //    })

  def graphFlowWithExtraSource: Flow[Message, Message, Unit] = {
    Flow() { implicit b =>
      import FlowGraph.Implicits._

      // Graph elements we'll use
      val merge = b.add(Merge[Int](2))
      val filter = b.add(Flow[Int].filter(_ => false))

      // convert to int so we can connect to merge
      val mapMsgToInt = b.add(Flow[Message].map[Int] { msg => -1 })
      val mapIntToMsg = b.add(Flow[Int].map[Message](x => TextMessage.Strict(":" + randomPrintableString(200) + ":" + x.toString)))
      val log = b.add(Flow[Int].map[Int](x => { println(x); x }))

      // source we want to use to send message to the connected websocket sink
      val rangeSource = b.add(Source(1 to 2000))

      // connect the graph
      mapMsgToInt ~> filter ~> merge // this part of the merge will never provide msgs
      rangeSource ~> log ~> merge ~> mapIntToMsg

      // expose ports
      (mapMsgToInt.inlet, mapIntToMsg.outlet)
    }
  }

  def randomPrintableString(length: Int, start: String = ""): String = {
    if (length == 0) start else randomPrintableString(length - 1, start + Random.nextPrintableChar())
  }

}
