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

  case class Props(cluster: DiscoveredCluster, selected: Boolean, select: String => Unit)

  case class State(selected: Boolean)

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      //      observe(t.state.nodes)
    }

    def selectCluster(e: ReactEvent) = {
      t.modState(_.copy(selected = true))
      t.props.select(t.props.cluster.system)
      e.preventDefault()
    }
  }

  val component = ReactComponentB[Props]("DiscoveredClusterComponent")
    .initialStateP(P => {
      State(P.selected)
    }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {

      a(href := "", onClick ==> B.selectCluster)(
        span(
          if (S.selected) {
            color := "red"
          } else {
            color := "blue"
          })(P.cluster.system)
      )

    }
    ).build

  def apply(cluster: DiscoveredCluster, selected: Boolean, select: String => Unit) = component(Props(cluster, selected, select))

}
