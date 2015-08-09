package clusterconsole.client.services

import clusterconsole.http.ClusterProtocol
import org.scalajs.dom
import org.scalajs.dom.raw._
import upickle.default._
import clusterconsole.http.Json._

object WebSocketClient {

  var open: Boolean = false

  lazy val websocket = new WebSocket(getWebsocketUri(dom.document))

  websocket.onopen = { (event: Event) =>
    ClusterService.findDiscoveringClusters()
    ClusterService.findDiscoveredClusters()
    event
  }
  websocket.onerror = { (event: ErrorEvent) => }

  websocket.onmessage = { (event: MessageEvent) =>
    val msg: ClusterProtocol = read[ClusterProtocol](event.data.toString)
    MainDispatcher.dispatch(msg)
    event
  }

  websocket.onclose = { (event: Event) => }

  def getWebsocketUri(document: Document): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${dom.document.location.host}/events"
  }

  def send(msg: ClusterProtocol): Unit = {
    websocket.send(write(msg))
  }

}
