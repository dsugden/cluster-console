package clusterconsole.client.components

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.{ Attrs, SvgAttrs }
import org.scalajs.dom.raw.SVGCircleElement
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
        circle(Attrs.cls := "node", Attrs.id := P.key, r := "20", cx := P.x, cy := P.y, fill := "#aaa", stroke := "#fff", strokeWidth := "1.px5"),
        text(x := P.x + 20, y := P.y - 20)("djhskfjhskdjf")
      )

    }.build

  def apply(node: GraphNodeForce, key: Int) = component(Props(node.x, node.y, key))
}

object GraphLink {

  case class Props(link: GraphLinkForce, key: Int)

  val component = ReactComponentB[Props]("GraphLink")
    .render { P =>
      line(
        Attrs.cls := "link",
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

    def dragMove(a: js.Any, b: Double): js.Any = {

      val mouse = d3.mouse(js.Dynamic.global.document.getElementById(b.toString))

      val newNodes: Rx[List[GraphNodeForce]] = Var(t.state.nodes().map(n =>
        if (n.index == b) {
          js.Dynamic.literal(
            "index" -> b,
            "x" -> mouse(0),
            "y" -> mouse(1),
            "px" -> n.px,
            "py" -> n.py,
            "fixed" -> n.fixed,
            "weight" -> n.weight
          ).asInstanceOf[GraphNodeForce]

        } else {
          n
        }
      ))

      val newLinks: Rx[List[GraphLinkForce]] = Var(t.state.links().map(link =>
        js.Dynamic.literal("source" -> newNodes()(link.source.index.toInt), "target" -> newNodes()(link.target.index.toInt)).asInstanceOf[GraphLinkForce]
      ))

      t.modState(s => s.copy(nodes = newNodes, links = newLinks))
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
      svgtag(SvgAttrs.width := P.width, SvgAttrs.height := P.height)(
        drawLinks(S.links),
        drawNodes(S.nodes)
      )
    }).componentWillMount { scope =>
      scope.backend.start()
    }.componentDidMount { scope =>
      scope.backend.mounted()

      val drag1 = d3.behavior.drag()
      drag1.origin(() => js.Array(0, 0)).on("drag", (a: js.Any, b: Double) => scope.backend.dragMove(a, b))

      //      val drag = scope.state.force.drag().on("dragstart", () => scope.backend.dragStart())

      d3.select("svg").selectAll(".node").call(drag1)

    }.componentWillUnmount { scope =>
      scope.state.force.stop()
    }.configure(OnUnmount.install).build

  def apply(width: Double, height: Double, nodes: List[GraphNodeForce], links: List[GraphLinkForce]) =
    component(Props(width, height, nodes, links))

}

