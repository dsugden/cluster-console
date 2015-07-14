package clusterconsole.client.services

import clusterconsole.client.services.Logger._
import clusterconsole.client.ukko.Actor
import clusterconsole.http.{ClusterProtocol, ClusterSubscribe}
import org.scalajs.dom
import org.scalajs.dom.raw._
import clusterconsole.http.Json._

object WebSocketClient {
  var open:Boolean  = false
  var clients:Seq[Actor] = Nil

  lazy val websocket = new WebSocket(getWebsocketUri(dom.document))

  websocket.onopen = { (event: Event) =>
    log.debug("***************  websocket.onopen ")
    event
  }
  websocket.onerror = { (event: ErrorEvent) =>
    log.debug("***************  websocket.onerror ")
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

    s"$wsProtocol://${dom.document.location.host}/events"
  }



  def subscribe(actor:Actor) = {
    clients = clients :+ actor
  }

  def send(msg:ClusterProtocol):Unit = {
    websocket.send(upickle.write(msg))
  }


}
