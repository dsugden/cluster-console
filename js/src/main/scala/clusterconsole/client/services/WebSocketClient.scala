package clusterconsole.client.services

import clusterconsole.client.services.Logger._
import clusterconsole.client.ukko.Actor
import clusterconsole.http.{ClusterProtocol, TestMessage}
import org.scalajs.dom
import org.scalajs.dom.raw._
import clusterconsole.http.Json._

object WebSocketClient {


  var open:Boolean  = false

  var clients:Seq[Actor] = Nil


  lazy val websocket = new WebSocket(getWebsocketUri(dom.document))

  websocket.onopen = { (event: Event) =>
    websocket.send(upickle.write(TestMessage("EE")))
    websocket.send(upickle.write(TestMessage("FF")))
    websocket.send(upickle.write(TestMessage("GG")))
    event
  }
  websocket.onerror = { (event: ErrorEvent) =>
  }
  websocket.onmessage = { (event: MessageEvent) =>


    log.debug("***************  on message " + event.data.toString)

    val msg:ClusterProtocol = upickle.read[ClusterProtocol](event.data.toString)

    log.debug("***************  on message " + msg)
    clients.foreach{ client =>

      MainDispatcher.dispatch( msg )

    }


    event

  }
  websocket.onclose = { (event: Event) =>
  }


  def getWebsocketUri(document: Document): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${dom.document.location.host}/api"
  }



  def subscribe(actor:Actor) = {

    clients = clients :+ actor


//    if(websocket.readyState == 1){
//
//    }else{
//      log.debug("*****************  websocket not open")
//    }

  }


}
