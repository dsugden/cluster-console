package clusterconsole.client.components

import clusterconsole.client.d3.D3.Selection
import clusterconsole.client.services.{ ClusterStore, ClusterStoreActions }
import clusterconsole.client.style.CustomTags._
import clusterconsole.http.DiscoveredCluster
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.{ Attrs, SvgAttrs }
import org.scalajs.dom
import org.scalajs.dom.raw.SVGCircleElement
import rx._

import clusterconsole.client.d3.Layout._
import clusterconsole.client.modules.RxObserver
import japgolly.scalajs.react.{ ReactComponentB, ReactNode }
import japgolly.scalajs.react._
//import japgolly.scalajs.react.vdom.all.svg._
//import japgolly.scalajs.react.vdom.prefix_<^._
import scala.scalajs.js
import clusterconsole.client.d3._
import js.JSConverters._
import clusterconsole.client.services.Logger._

import japgolly.scalajs.react.vdom.all._
import scala.scalajs.js

object GraphD3 {

  case class Props(width: Double, height: Double, nodes: List[GraphNode], links: List[GraphLink])

  case class State(forceLayout: ForceLayout)

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      //      observe(t.state.nodes)
    }

  }

  val component = ReactComponentB[Props]("GraphD3")
    .initialStateP { P =>
      val force = d3.layout.force()
        .size(List[Double](P.width, P.height).toJsArray)
        .charge(-600)
        .linkDistance(200)
        .friction(0.3)

      State(force)

    }.backend(new Backend(_))
    .render((P, S, B) => {
      //      svgtag(SvgAttrs.width := P.width, SvgAttrs.height := P.height)
      div(cls := "svgparent")
    }).componentDidMount { scope =>
      scope.backend.mounted()

      val svg = d3.select(".svgparent").append("svg")
        .attr("width", scope.props.width)
        .attr("height", scope.props.width)

      //      scope.state.forceLayout.nodes(scope.props.nodes.toJsArray).links(scope.props.links.toJsArray).start()
      //
      //      val link: Selection = svg.selectAll(".link")
      //        .data(scope.props.links.toJsArray)
      //        .enter().append("line")
      //        .attr("class", "link")
      //      //      .style("stroke-width",(d: GraphLink, i: Double) => {
      //      //      val w = scala.math.sqrt(d.value)
      //      //      w.asInstanceOf[js.Dynamic]
      //      //    })
      //
      //      log.debug("Link made")
      //
      //      val node: D3.Selection = svg.selectAll(".node")
      //        .data(scope.props.nodes.toJsArray)
      //        .enter().append("circle")
      //        .attr("class", "node")
      //        .attr("r", 5)
      //        .style("fill", (d: GraphNode, i: Double) => {
      //          "#ccc"
      //        }).call(scope.state.forceLayout.drag())
      //
      //      def tickFunc = () => {
      //        link.attr("x1", (d: GraphLink) => d.source.x)
      //          .attr("y1", (d: GraphLink) => d.source.y)
      //          .attr("x2", (d: GraphLink) => d.target.x)
      //          .attr("y2", (d: GraphLink) => d.target.y)
      //
      //        node.attr("cx", (d: GraphNode) => d.x)
      //          .attr("cy", (d: GraphNode) => d.y)
      //        ()
      //      }
      //
      //      scope.state.forceLayout.on("tick", tickFunc)
      //
    }.build

  def apply(system: String, width: Double, height: Double, nodes: List[GraphNode], links: List[GraphLink]) = {

    component(Props(width, height, nodes, links))
  }

}
