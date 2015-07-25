package clusterconsole.client.components

import clusterconsole.client.components.ClusterFormComponent.{ EditClusterProps, State }
import clusterconsole.client.components.Graph.State
import clusterconsole.client.components.Graph.State
import clusterconsole.client.d3._
import clusterconsole.client.modules.RxObserver
import clusterconsole.client.services.{ ClusterStoreActions, ClusterStore }
import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{ ClusterForm, HostPort, DiscoveredCluster }
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import clusterconsole.client.services.Logger._
import rx._

import scala.scalajs.js

object DiscoveredClusterComponent {

  case class Props(cluster: DiscoveredCluster)

  case class State()

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      //      observe(t.state.nodes)
    }

    def toggleCluster() = {

    }

  }

  val component = ReactComponentB[Props]("DiscoveredClusterComponent")
    .initialStateP(P => {
      State()
    }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {

      a(P.cluster.name, onClick --> B.toggleCluster())

    }
    ).build

  def apply(cluster: DiscoveredCluster) = component(Props(cluster))

}
