package clusterconsole.client.components

import clusterconsole.client.modules.RxObserver
import clusterconsole.client.services.ClusterStoreActions
import clusterconsole.http.{ DiscoveryBegun, DiscoveredCluster }
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.all._
import rx._

object DiscoveringClusterComponent {

  case class Props(discovering: Rx[Map[String, DiscoveryBegun]])

  case class State()

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      observe(t.props.discovering)
      //      ClusterStoreActions.getDiscoveringClusters()
    }

  }

  val component = ReactComponentB[Props]("DiscoveringClusterComponent")
    .initialStateP(P => {
      State()
    }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {
      div(
        h3("Discovering Clusters"),
        div(
          P.discovering().values.map(e =>
            div(key := e.system)(
              span(e.system) + " " + span(e.seedNodes.map(hp => hp.host + ":" + hp.port))
            )
          )
        )
      )
    }
    ).componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def apply(discovering: Rx[Map[String, DiscoveryBegun]]) = component(Props(discovering))

}
