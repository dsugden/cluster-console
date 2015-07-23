package clusterconsole.client.components

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
      circle(r := 20, cx := P.x, cy := P.y, fill := "#aaa", stroke := "#fff", strokeWidth := "1.px5", "key".reactAttr := P.key)
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

      log.debug("------------ initialStateP")

      val force = d3.layout.force()
        .size(List[Double](P.width, P.height).toJsArray)
        .charge(-400)
        .linkDistance(40)

      State(Var(P.nodes), Var(P.links), force)

    }.backend(new Backend(_))
    .render((P, S, B) => {

      log.debug("**************** render")

      svgtag(width := P.width, height := P.height)(
        drawLinks(S.links),
        drawNodes(S.nodes)
      )
    }).componentDidMount { scope =>

      scope.backend.mounted()
      scope.backend.start()

    }.build

  def apply(width: Double, height: Double, nodes: List[GraphNodeForce], links: List[GraphLinkForce]) =
    component(Props(width, height, nodes, links))

}

/*
color = d3.scale.category20();

var Node = React.createClass({
  render: function () {
    return (
        <circle
          r={5}
          cx={this.props.x}
          cy={this.props.y}
          style={{
            "fill": color(this.props.group),
            "stroke":"#fff",
            "strokeWidth":"1.5px"
          }}/>
    )
  }
});

var Link = React.createClass({

  render: function () {
    return (
      <line
        x1={this.props.datum.source.x}
        y1={this.props.datum.source.y}
        x2={this.props.datum.target.x}
        y2={this.props.datum.target.y}
        style={{
          "stroke":"#999",
          "strokeOpacity":".6",
          "strokeWidth": Math.sqrt(this.props.datum.value)
        }}/>
    );
  }
})

var Graph = React.createClass({
    mixins: [Radium.StyleResolverMixin, Radium.BrowserStateMixin],
    getInitialState: function() {

    var svgWidth = 900;
    var svgHeight = 900;
    var force = d3.layout.force()
      .charge(-120)
      .linkDistance(30)
      .size([svgWidth, svgHeight]);

      return {
        svgWidth: svgWidth,
        svgHeight: svgHeight,
        force: force,
        nodes: null,
        links: null
      }
    },
    componentDidMount: function () {
      var self = this;
      // refactor entire graph into sub component - force layout shouldn't be
      // manipulating props, though this works
      this.state.force
                .nodes(this.props.lesmis.nodes)
                .links(this.props.lesmis.links)
                .start()
      this.state.force.on("tick", function (tick, b, c) {
        self.forceUpdate()
      })
    },
    drawLinks: function () {
      var links = this.props.lesmis.links.map(function (link, index) {
        return (<Link datum={link} key={index} />)
      })
      return (<g>
        {links}
      </g>)
    },
    drawNodes: function () {
      var nodes = this.props.lesmis.nodes.map(function (node, index) {
        return (<Node
          key={index}
          x={node.x}
          y={node.y}
          group={node.group}/>
        ) })
      return nodes;
    },
    render: function() {
        return (
          <div>
            <div style={{"marginLeft": "20px", "fontFamily": "Helvetica"}}>

            </div>
            <svg
              style={{"border": "2px solid black", "margin": "20px"}}
              width={this.state.svgWidth}
              height={this.state.svgHeight}>
              {this.drawLinks()}
              {this.drawNodes()}
            </svg>
          </div>
        )
    }
});

d3.json("https://gist.githubusercontent.com/fredbenenson/4212290/raw/40be75727ab60227a2b41abe5a509d30de831ffd/miserables.json", function(error, lesmis) {
  React.render(<Graph lesmis={lesmis}/>, document.getElementById("mount-point"));
});

 */

