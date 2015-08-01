package clusterconsole.client.components

import clusterconsole.client.components.ClusterFormComponent.{ EditClusterProps, State }
import clusterconsole.client.components.Graph.State
import clusterconsole.client.components.Graph.State
import clusterconsole.client.d3._
import clusterconsole.client.modules.RxObserver
import clusterconsole.client.services.{ ClusterStoreActions, ClusterStore }
import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{ DiscoveryBegun, ClusterForm, HostPort, DiscoveredCluster }
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import clusterconsole.client.services.Logger._
import rx._

import scala.scalajs.js

object DiscoveredClusterComponent {

  case class Props(discovered: Rx[Map[String, DiscoveredCluster]], selected: Rx[Option[DiscoveredCluster]])

  case class State()

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      observe(t.props.discovered)
      observe(t.props.selected)

    }

    def selectCluster(e: ReactMouseEvent) = {
      ClusterStoreActions.selectCluster(e.currentTarget.firstChild.nodeValue)
      e.preventDefault()
    }

  }

  val component = ReactComponentB[Props]("DiscoveredClusterComponent")
    .initialStateP(P => {
      State()
    }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {
      if (P.discovered().isEmpty) {
        span("")
      } else {
        div(cls := "row")(
          div(cls := "col-md-12")(
            div(cls := "row", backgroundColor := "#02631B")(div(cls := "col-md-12")(h4("Discovered Clusters"))),
            div(cls := "row", backgroundColor := "#666")(div(cls := "col-md-12")(
              P.discovered().values.map(e =>
                a(href := "", key := e.system)(
                  span(onClick ==> B.selectCluster)(
                    color := P.selected().map(dc =>
                      if (dc.system == e.system) {
                        "red"
                      } else {
                        "blue"
                      }))(e.system)
                )
              )))
          )
        )
      }
    }
    ).componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def apply(discovered: Rx[Map[String, DiscoveredCluster]], selected: Rx[Option[DiscoveredCluster]]) = component(Props(discovered, selected))

}
