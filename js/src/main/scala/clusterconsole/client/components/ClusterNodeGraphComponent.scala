package clusterconsole.client.components

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

  case class Props(store: ClusterStore, system: String)

  case class State(system: Option[DiscoveredCluster], linkMode: LinkMode)

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      observe(t.props.store.getDiscoveredClusters)
      observe(t.props.store.getDiscoveredClusterNodes)
    }

  }

  def component = ReactComponentB[Props]("ClusterNodeGraph")
    .initialStateP(P => {
      State(P.store.getSelectedCluster(), Cluster)
    }).backend(new Backend(_))
    .render((P, S, B) => {

      log.debug("********  render ClusterNodeGraphComponent")
      log.debug("********  render P.store.getDiscoveredClusterNodes() " + P.store.getDiscoveredClusterNodes())

      S.system.fold(span(""))(cluster => {

        val fixedMap = P.store.getDiscoveredClusterNodes().get(cluster.system)

        val nodes: List[GraphNode] = fixedMap.getOrElse(
          cluster.members.toList.zipWithIndex.map {
            case (node, i) =>
              js.Dynamic.literal(
                "name" -> node.label,
                "index" -> i,
                "x" -> 450,
                "y" -> 450,
                "px" -> 0,
                "py" -> 0,
                "fixed" -> false,
                "weight" -> 0
              ).asInstanceOf[GraphNode]
          })

        val indexes = nodes.map(_.index)

        val links: List[GraphLink] =
          indexes.flatMap(f => indexes.filter(_ > f).map((f, _))).map {
            case (a, b) =>

              val nodeA = nodes(a.toInt)

              js.Dynamic.literal("source" -> nodes(a.toInt), "target" -> nodes(b.toInt)).asInstanceOf[GraphLink]

          }
        div(
          Graph(cluster.system, 900, 900, nodes, links, fixedMap.isDefined)
        )

      })

    }).componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def apply(store: ClusterStore, system: String) = component(Props(store, system))

}
