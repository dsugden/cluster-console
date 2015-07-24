package clusterconsole.client.components

import japgolly.scalajs.react.extra.OnUnmount
import rx._

import clusterconsole.client.d3.Layout._
import clusterconsole.client.modules.RxObserver
import japgolly.scalajs.react.{ ReactComponentB, ReactNode }
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all.svg._
import japgolly.scalajs.react.vdom.prefix_<^._
import scala.scalajs.js
import clusterconsole.client.d3._
import js.JSConverters._
import clusterconsole.client.services.Logger._

import scala.scalajs.js

case class Node(name: String)

case class Link(source: Int, target: Int)

trait LinkData extends GraphLinkForce {
  var value: Double = js.native
}

trait NodeData extends GraphNodeForce {
  var group: Int = js.native
}

trait GraphData extends js.Object {
  var nodes: js.Array[NodeData] = js.native
  var links: js.Array[LinkData] = js.native
}

object GraphNode {

  case class Props(x: Double, y: Double, key: Int)

  val component = ReactComponentB[Props]("GraphNode")
    .render { P =>
      g(
        circle(r := 20, cx := P.x, cy := P.y, fill := "#aaa", stroke := "#fff", strokeWidth := "1.px5", "key".reactAttr := P.key),
        text(x := P.x - 10, y := P.y - 10)("djhskfjhskdjf")
      )

    }.build

  def apply(node: GraphNodeForce, key: Int) = component(Props(node.x, node.y, key))
}

object GraphLink {

  case class Props(link: GraphLinkForce, key: Int)

  val component = ReactComponentB[Props]("GraphLink")
    .render { P =>
      line(
        "key".reactAttr := P.key,
        x1 := P.link.source.x,
        y1 := P.link.source.y,
        x2 := P.link.target.x,
        y2 := P.link.target.y,
        stroke := "#999",
        strokeOpacity := "0.6",
        strokeWidth := "1")
    }.build

  def apply(link: GraphLinkForce, key: Int) = component(Props(link, key))
}

object Graph {

  import clusterconsole.client.style.CustomTags._

  case class Props(width: Double, height: Double, nodes: List[GraphNodeForce], links: List[GraphLinkForce])

  case class State(nodes: Rx[List[GraphNodeForce]], links: Rx[List[GraphLinkForce]], force: ForceLayout)

  def drawLinks(links: Rx[List[GraphLinkForce]]): ReactNode =
    g(links().zipWithIndex.map { case (eachLink, i) => GraphLink(eachLink, i) })

  def drawNodes(nodes: Rx[List[GraphNodeForce]]): List[ReactNode] =
    nodes().zipWithIndex.map {
      case (node, i) =>
        GraphNode(node, i)
    }

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      observe(t.state.nodes)
    }

    def tick() = {
      val newNodes: Rx[List[GraphNodeForce]] = Var(t.state.force.nodes().toList)
      t.modState(s => s.copy(nodes = newNodes))
    }

    def start() = {
      t.modState { s =>
        val firstState = s.copy(force = s.force.nodes(t.props.nodes.toJsArray).start())
        firstState.copy(force = s.force.on("tick", () => tick))
      }
    }
  }

  val component = ReactComponentB[Props]("Graph")
    .initialStateP { P =>
      val force = d3.layout.force()
        .size(List[Double](P.width, P.height).toJsArray)
        .charge(-600)
        .linkDistance(40)

      State(Var(P.nodes), Var(P.links), force)

    }.backend(new Backend(_))
    .render((P, S, B) => {
      svgtag(width := P.width, height := P.height)(
        drawLinks(S.links),
        drawNodes(S.nodes)
      )
    }).componentWillMount { scope =>
      scope.backend.start()
    }.componentDidMount { scope =>
      scope.backend.mounted()
    }.componentWillUnmount { scope =>
      scope.state.force.stop()
    }.configure(OnUnmount.install).build

  def apply(width: Double, height: Double, nodes: List[GraphNodeForce], links: List[GraphLinkForce]) =
    component(Props(width, height, nodes, links))

}

