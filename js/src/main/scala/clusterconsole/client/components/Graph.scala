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

object GraphNode {

  case class Props(node: GraphNode)

  val component = ReactComponentB[Props]("GraphNode")
    .render { P =>
      g(
        circle(Attrs.cls := "node", Attrs.id := P.node.index, r := "20", cx := P.node.x, cy := P.node.y, fill := "#aaa", stroke := "#fff", strokeWidth := "1.px5"),
        text(x := P.node.x + 20, y := P.node.y - 20)(P.node.name)
      )

    }.build

  def apply(node: GraphNode) = component(Props(node))
}

object GraphLink {

  case class Props(link: GraphLink, key: Int)

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

  def apply(link: GraphLink, key: Int) = component(Props(link, key))
}

object Graph {

  import clusterconsole.client.style.CustomTags._

  case class Props(width: Double, height: Double, nodes: List[GraphNode], links: List[GraphLink])

  case class State(nodes: Rx[List[GraphNode]], links: Rx[List[GraphLink]], force: ForceLayout)

  def drawLinks(links: Rx[List[GraphLink]]): ReactNode =
    g(links().zipWithIndex.map { case (eachLink, i) => GraphLink(eachLink, i) })

  def drawNodes(nodes: Rx[List[GraphNode]]): List[ReactNode] =
    nodes().zipWithIndex.map {
      case (node, i) =>
        GraphNode(node)
    }

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      observe(t.state.nodes)
    }

    def tick() = {
      val newNodes: Rx[List[GraphNode]] = Var(t.state.force.nodes().toList)
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

      val newNodes: Rx[List[GraphNode]] = Var(t.state.nodes().map(n =>
        if (n.index == b) {
          js.Dynamic.literal(
            "name" -> n.name,
            "index" -> b,
            "x" -> mouse(0),
            "y" -> mouse(1),
            "px" -> n.px,
            "py" -> n.py,
            "fixed" -> n.fixed,
            "weight" -> n.weight
          ).asInstanceOf[GraphNode]

        } else {
          n
        }
      ))

      val newLinks: Rx[List[GraphLink]] = Var(t.state.links().map(link =>
        js.Dynamic.literal("source" -> newNodes()(link.source.index.toInt), "target" -> newNodes()(link.target.index.toInt)).asInstanceOf[GraphLink]
      ))

      t.modState(s => s.copy(nodes = newNodes, links = newLinks))
    }

  }

  val component = ReactComponentB[Props]("Graph")
    .initialStateP { P =>
      val force = d3.layout.force()
        .size(List[Double](P.width, P.height).toJsArray)
        .charge(-600)
        .linkDistance(80)

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
      d3.select("svg").selectAll(".node").call(drag1)

    }.componentWillUnmount { scope =>
      scope.state.force.stop()
    }.configure(OnUnmount.install).build

  def apply(width: Double, height: Double, nodes: List[GraphNode], links: List[GraphLink]) =
    component(Props(width, height, nodes, links))

}

