package clusterconsole.client.components

import clusterconsole.client.d3.D3.Selection
import clusterconsole.client.services.{ ClusterStore, ClusterStoreActions }
import clusterconsole.http.DiscoveredCluster
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

trait ClusterGraphNode extends GraphNode {
  var host: String = js.native
  var roles: String = js.native
  var status: String = js.native
}

trait ClusterGraphLink extends GraphLink {
  var sourceHost: String = js.native
  var targetHost: String = js.native

}

object GraphNode {

  case class Props(node: ClusterGraphNode, force: ForceLayout)

  case class State()

  val component = ReactComponentB[Props]("GraphNode")
    .render { P =>
      g(
        circle(Attrs.cls := "node", Attrs.id := P.node.index, r := "40", cx := P.node.x, cy := P.node.y,
          fill := {

            // TODO match on enum
            P.node.status match {
              case "Up" => "#3ACC35"
              case "Unreachable" => "#F26F11"
              case "Removed" => "#F21111"
              case "Exited" => "#F21111"
            }

          }, stroke := "#fff", strokeWidth := "1.px5"),
        text(x := P.node.x - 30, y := P.node.y - 65, fill := "white")(P.node.host),
        text(x := P.node.x - 30, y := P.node.y - 45, fill := "green")(P.node.roles),
        text(x := P.node.x - 30, y := P.node.y - 25, fill := "green")(P.node.status)
      )

    }.componentDidMount { scope =>

      val drag1 = d3.behavior.drag()
      //      drag1.origin(() => js.Array(0, 0))
      //      d3.select(scope.getDOMNode().firstChild).call(scope.props.force.drag())
      d3.select(scope.getDOMNode().firstChild).call(drag1)

    }.build

  def apply(node: ClusterGraphNode, force: ForceLayout) = component(Props(node, force))
}

object GraphLink {

  case class Props(link: ClusterGraphLink, key: Int)

  val component = ReactComponentB[Props]("GraphLink")
    .render { P =>
      line(
        Attrs.cls := "link",
        x1 := P.link.source.x,
        y1 := P.link.source.y,
        x2 := P.link.target.x,
        y2 := P.link.target.y,
        stroke := "#999",
        strokeOpacity := "1",
        strokeWidth := "1")
    }.build

  def apply(link: ClusterGraphLink, key: Int) = component(Props(link, key))
}

object Graph {

  import clusterconsole.client.style.CustomTags._

  case class Props(system: String, width: Double, height: Double,
    store: ClusterStore, fixedMap: Boolean)

  case class State(nodes: List[ClusterGraphNode],
    links: List[ClusterGraphLink],
    force: ForceLayout)

  def drawLinks(links: List[ClusterGraphLink]): ReactNode =
    g(links.zipWithIndex.map { case (eachLink, i) => GraphLink(eachLink, i) })

  def drawNodes(nodes: List[ClusterGraphNode], force: ForceLayout): List[ReactNode] =
    nodes.zipWithIndex.map {
      case (node, i) =>
        GraphNode(node, force)
    }

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      //      observe(t.props.store.getSelectedCluster)
      react(t.props.store.getSelectedCluster, updateGraph)
    }

    def updateGraph(c: Option[DiscoveredCluster]) = {

      c.fold[Unit]({})(cluster => {

        //        val fixedMap = P.store.getDiscoveredClusterNodes().get(cluster.system)

        val currentNodesMap = t.state.nodes.map(e => (e.host, e)).toMap

        val existingIndexes = t.state.nodes.map(_.index).toSet

        val incomingNodes: List[ClusterGraphNode] =
          cluster.members.toList.map { node =>

            currentNodesMap.get(node.address.label).fold(
              js.Dynamic.literal(
                "host" -> node.address.label,
                "roles" -> node.roles.mkString(","),
                "status" -> node.state.toString,
                "name" -> node.label,
                "index" -> getNewIndex(existingIndexes, 1),
                "x" -> 450,
                "y" -> 450,
                "px" -> 450,
                "py" -> 450,
                "fixed" -> false,
                "weight" -> 0
              ).asInstanceOf[ClusterGraphNode]
            )(cn =>

                js.Dynamic.literal(
                  "host" -> node.address.label,
                  "roles" -> node.roles.mkString(","),
                  "status" -> node.state.toString,
                  "name" -> node.label,
                  "index" -> cn.index,
                  "x" -> cn.x,
                  "y" -> cn.y,
                  "px" -> cn.px,
                  "py" -> cn.py,
                  "fixed" -> cn.fixed,
                  "weight" -> cn.weight
                ).asInstanceOf[ClusterGraphNode]
              )

          }

        t.modState { s =>

          log.debug("******** newNodes " + incomingNodes.map(_.host))

          val indexes = incomingNodes.filter(_.status == "Up").map(_.index)

          log.debug("******** indexes " + indexes)

          val links: List[ClusterGraphLink] =
            indexes.flatMap(index => indexes.filter(_ > index).map((index, _))).flatMap {
              case (a, b) =>
                for {
                  source <- incomingNodes.find(_.index == a)
                  target <- incomingNodes.find(_.index == b)
                } yield {
                  js.Dynamic.literal(
                    "source" -> source,
                    "target" -> target,
                    "sourceHost" -> source.host,
                    "targetHost" -> target.host).asInstanceOf[ClusterGraphLink]
                }
            }

          log.debug("links = " + links.map(e => e.sourceHost + " " + e.targetHost))

          val nodeUpdateState = s.copy(nodes = incomingNodes, force = s.force.nodes(incomingNodes.toJsArray).start())

          val linkUpdateState = nodeUpdateState.copy(links = links)

          linkUpdateState
        }

      })
    }

    /*
      c.fold[Unit]({})(cluster => {
        val (nodes, links) = getNodesAndLink(cluster)

        val newState = State(nodes, links, t.state.force)

        t.modState { s =>
          val firstState = s.copy(nodes = nodes, links = links, force = s.force.nodes(nodes.toJsArray).start())
          firstState.copy(force = s.force.on("tick", () => tick))
          //          firstState.copy(force = s.force.on("tick", () => tick))
        }
      })

     */

    def tick() = {
      val newNodes: List[ClusterGraphNode] = t.state.force.nodes().toList
      t.modState(s => s.copy(nodes = newNodes))
    }

    def start() = {
      t.modState { s =>
        val firstState = s.copy(force = s.force.nodes(t.state.nodes.toJsArray).start())
        firstState.copy(force = s.force.on("tick", () => tick))
      }
    }

    def resume() = {
      t.modState { s =>
        val firstState = s.copy(force = s.force.nodes(t.state.nodes.toJsArray).resume())
        firstState.copy(force = s.force.on("tick", () => tick))
      }

    }
  }

  //    def dragEnd(a: js.Any, b: Double): js.Any = {
  //      val mouse = d3.mouse(js.Dynamic.global.document.getElementById(b.toString))
  //
  //      val newNodes: List[GraphNode] = t.state.nodes.map(n =>
  //        if (n.index == b) {
  //          js.Dynamic.literal(
  //            "name" -> n.name,
  //            "index" -> b,
  //            "x" -> mouse(0),
  //            "y" -> mouse(1),
  //            "px" -> n.px,
  //            "py" -> n.py,
  //            "fixed" -> true,
  //            "weight" -> n.weight
  //          ).asInstanceOf[GraphNode]
  //
  //        } else {
  //          n
  //        }
  //      )
  //
  //      ClusterStoreActions.updateClusterNode(t.props.system, newNodes)
  //
  //    }

  //    def dragMove(a: js.Any, b: Double): js.Any = {
  //
  //      val mouse = d3.mouse(js.Dynamic.global.document.getElementById(b.toString))
  //
  //      val newNodes: List[ClusterGraphNode] = t.state.nodes.map(n =>
  //        if (n.index == b) {
  //          js.Dynamic.literal(
  //            "host" -> n.host,
  //            "name" -> n.name,
  //            "index" -> b,
  //            "x" -> mouse(0),
  //            "y" -> mouse(1),
  //            "px" -> n.px,
  //            "py" -> n.py,
  //            "fixed" -> true,
  //            "weight" -> n.weight
  //          ).asInstanceOf[ClusterGraphNode]
  //
  //        } else {
  //          n
  //        }
  //      )
  //
  //      val newLinks: List[GraphLink] = t.state.links.map(link =>
  //        js.Dynamic.literal("source" -> newNodes(link.source.index.toInt), "target" -> newNodes(link.target.index.toInt)).asInstanceOf[GraphLink]
  //      )
  //
  //      t.modState(s => s.copy(nodes = newNodes, links = newLinks))
  //
  //    }
  //
  //  }

  val component = ReactComponentB[Props]("Graph")
    .initialStateP { P =>

      val force = d3.layout.force()
        .size(List[Double](P.width, P.height).toJsArray)
        .charge(-1500)
        .chargeDistance(1000)
        .linkDistance(500)
      //        .friction(0.9)

      val (nodes, links) = P.store.getSelectedCluster().map(cluster => {
        getNodesAndLink(cluster)
      }).getOrElse((Nil, Nil))
      //
      //      val nodeSelection: Selection = d3.select("svg").selectAll(".node")
      //      val linkSelection: Selection = d3.select("svg").selectAll(".link")
      //
      State(nodes, links, force)

    }.backend(new Backend(_))
    .render((P, S, B) => {

      log.debug("@@@@@@@@@@@@@@@@  render Graph")
      //
      //      log.debug("@@@@@@@@@@@@@@@@  render Graph S.nodes = " + S.nodes().map(_.name))
      //
      svgtag(SvgAttrs.width := P.width, SvgAttrs.height := P.height)(
        drawLinks(S.links),
        drawNodes(S.nodes, S.force)
      )
    }).componentWillReceiveProps { (scope, P) =>
      val (nodes, links) = P.store.getSelectedCluster().map(cluster => {
        getNodesAndLink(cluster)
      }).getOrElse((Nil, Nil))

      val newState = State(nodes, links, scope.state.force)

      log.debug("$$$$$$$$$$$$$$$$$$$  render componentWillReceiveProps S: " + newState.nodes.map(_.name))

      scope.modState { s =>
        val firstState = s.copy(nodes = nodes, links = links, force = s.force.nodes(nodes.toJsArray).start())
        firstState.copy(force = s.force.on("tick", () => scope.backend.tick))
      }

    }.componentWillMount { scope =>
      if (!scope.props.fixedMap) {
        log.debug("Graph scope.backend.start()")
        scope.backend.start()
      }

    }.componentDidMount { scope =>
      scope.backend.mounted()
      //
      //      val drag1 = d3.behavior.drag()
      //      drag1.origin(() => js.Array(0, 0)).on("drag", (a: js.Any, b: Double) => scope.backend.dragMove(a, b))
      //        .on("dragend", (a: js.Any, b: Double) => scope.backend.dragEnd(a, b))
      //      d3.select("svg").selectAll(".node").call(drag1)

    }.componentWillUnmount { scope =>

      log.debug("Graph unmounting")
      scope.state.force.stop()
    }.configure(OnUnmount.install).build

  def apply(system: String, width: Double, height: Double, store: ClusterStore, fixedMap: Boolean) = {

    component(Props(system, width, height, store, fixedMap))
  }

  def getNodesAndLink(cluster: DiscoveredCluster): (List[ClusterGraphNode], List[ClusterGraphLink]) = {
    val nodes = cluster.members.toList.zipWithIndex.map {
      case (node, i) =>
        js.Dynamic.literal(
          "host" -> node.address.label,
          "roles" -> node.roles.mkString(","),
          "status" -> node.state.toString,
          "name" -> node.label,
          "index" -> i,
          "x" -> 200,
          "y" -> 200,
          "px" -> 200,
          "py" -> 200,
          "fixed" -> false,
          "weight" -> 0
        ).asInstanceOf[ClusterGraphNode]
    }
    val indexes = nodes.filter(_.status == "Up").map(_.index)

    log.debug("******** indexes " + indexes)

    val links: List[ClusterGraphLink] =
      indexes.flatMap(index => indexes.filter(_ > index).map((index, _))).flatMap {
        case (a, b) =>
          for {
            source <- nodes.find(_.index == a)
            target <- nodes.find(_.index == b)
          } yield {
            js.Dynamic.literal(
              "source" -> source,
              "target" -> target,
              "sourceHost" -> source.host,
              "targetHost" -> target.host).asInstanceOf[ClusterGraphLink]
          }
      }

    log.debug("**************  nodes: " + nodes.map(_.name))
    (nodes, links)

  }

  def getNewIndex(set: Set[Double], v: Double): Double =
    if (set.contains(v)) {
      getNewIndex(set, v + 1)
    } else {
      v
    }

}

