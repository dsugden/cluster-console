package clusterconsole.client.components

import clusterconsole.client.modules.{ Mode, RxObserver }
import clusterconsole.client.services.{ ClusterService, ClusterService$ }
import clusterconsole.http.DiscoveredCluster
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
object ClusterNodeGraphComponent {

  case class Props(store: ClusterService, mode: Mode)

  case class State(cluster: Option[DiscoveredCluster])

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
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
      State(P.store.getSelectedCluster())
    }).backend(new Backend(_))
    .render((P, S, B) => {
      S.cluster.fold(span(""))(cluster => {
        div(
          Graph(cluster.system, P.mode, 1400, 900, P.store, false)
        )
      })
    }).componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def apply(store: ClusterService, mode: Mode) = component(Props(store, mode))

}
