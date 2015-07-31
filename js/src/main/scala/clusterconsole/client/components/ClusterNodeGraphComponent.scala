package clusterconsole.client.components

import clusterconsole.client.components.Graph
import clusterconsole.client.modules.RxObserver
import clusterconsole.client.services.ClusterStore
import clusterconsole.http.DiscoveredCluster
import japgolly.scalajs.react.extra.OnUnmount
import rx._
import clusterconsole.client.d3.Layout.{ GraphLink, GraphNode }
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._

//import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js
import clusterconsole.client.d3._
import js.JSConverters._
import clusterconsole.client.services.Logger._

sealed trait LinkMode

case object Cluster extends LinkMode

case object Roles extends LinkMode

object ClusterNodeGraphComponent {

  case class Props(store: ClusterStore)

  case class State(cluster: Option[DiscoveredCluster], linkMode: LinkMode)

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      //      observe(t.props.store.getSelectedCluster)

      react(t.props.store.getSelectedCluster, updateCluster)

    }

    def updateCluster(maybeCluster: Option[DiscoveredCluster]) = {

      val current = t.state.cluster

      current match {
        case None => maybeCluster.fold[Unit]({})(c => t.modState(_.copy(cluster = maybeCluster)))
        case Some(c) => maybeCluster.fold[Unit](t.modState(_.copy(cluster = None)))(newC =>
          if (c.system != newC.system) {
            t.modState(_.copy(cluster = maybeCluster))
          })
      }

    }

  }

  def component = ReactComponentB[Props]("ClusterNodeGraph")
    .initialStateP(P => {
      State(P.store.getSelectedCluster(), Cluster)
    }).backend(new Backend(_))
    .render((P, S, B) => {
      S.cluster.fold(span(""))(cluster => {
        div(
          Graph(cluster.system, 900, 900, P.store, false)
        )
      })
    }).componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def apply(store: ClusterStore) = component(Props(store))

}
