package clusterconsole.client.components

import clusterconsole.client.modules.RxObserver
import clusterconsole.client.services.ClusterStore
import japgolly.scalajs.react.extra.OnUnmount
import rx._
import clusterconsole.client.d3.Layout.{ GraphLinkForce, GraphNodeForce }
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._
//import japgolly.scalajs.react.vdom.prefix_<^._
import scala.scalajs.js
import clusterconsole.client.d3._
import js.JSConverters._
import clusterconsole.client.services.Logger._

trait ClusterNode extends GraphNodeForce {
  var name: String = js.native
}

object ClusterNodeGraphComponent {

  case class Props(store: ClusterStore, system: String)

  case class State()
  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      observe(t.props.store.getDiscoveredClusters)
    }

  }

  def component = ReactComponentB[Props]("ClusterNodeGraph")
    .initialStateP(P => {
      State()
    }).backend(new Backend(_))
    .render((P, S, B) => {

      val cluster = P.store.getDiscoveredClusters().get(P.system)
      cluster.fold(span(""))(cluster => {

        log.debug("*****************  " + cluster.members.toList)

        val nodes: List[GraphNodeForce] =
          cluster.members.toList.zipWithIndex.map {
            case (node, i) =>
              js.Dynamic.literal(
                //                "name" -> node.label,
                "index" -> i,
                "x" -> 0,
                "y" -> 0,
                "px" -> 0,
                "py" -> 0,
                "fixed" -> false,
                "weight" -> 0
              ).asInstanceOf[GraphNodeForce]
          }

        val links: List[GraphLinkForce] = Nil
        //        List(Link(0, 1), Link(0, 2), Link(1, 2)).zipWithIndex.map {
        //          case (link, i) =>
        //            js.Dynamic.literal("source" -> nodes(link.source), "target" -> nodes(link.target)).asInstanceOf[GraphLinkForce]
        //        }
        div(
          Graph(600, 600, nodes, links)
        )

      })

    }).componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def apply(store: ClusterStore, system: String) = component(Props(store, system))

}
