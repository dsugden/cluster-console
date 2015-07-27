package clusterconsole.client.components

import clusterconsole.client.modules.RxObserver
import clusterconsole.http.{ DiscoveryBegun, DiscoveredCluster }
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._

object DiscoveringClusterComponent {

  case class Props(cluster: DiscoveryBegun)

  case class State()

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      //      observe(t.state.nodes)
    }

    def toggleCluster() = {

    }

  }

  val component = ReactComponentB[Props]("DiscoveringClusterComponent")
    .initialStateP(P => {
      State()
    }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {

      a(href := "#", onClick --> B.toggleCluster())(
        span(P.cluster.system) + " " + span(P.cluster.seedNodes.map(hp => hp.host + ":" + hp.port))
      )

    }
    ).build

  def apply(cluster: DiscoveryBegun) = component(Props(cluster))

}
