package clusterconsole.client.components

import clusterconsole.client.components.GraphNode.State
import clusterconsole.client.d3.D3.Selection
import clusterconsole.client.services.{ ClusterStore, ClusterStoreActions }
import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{ ClusterDependency, RoleDependency, DiscoveredCluster }
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.{ Attrs, SvgAttrs }
import org.scalajs.dom.raw.SVGCircleElement
import rx._

import clusterconsole.client.d3.Layout._
import clusterconsole.client.modules._
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

object ClusterDependencyLegend {

  case class Props(dep: RoleDependency, index: Int, selectDep: (RoleDependency, Boolean) => Unit)

  case class State(selected: Boolean)

  class Backend(t: BackendScope[Props, State]) {
    def select = {
      t.modState(_.copy(selected = !t.state.selected))
      t.props.selectDep(t.props.dep, !t.state.selected)
    }
  }

  val component = ReactComponentB[Props]("ClusterDependencyLegend")
    .initialStateP(P => State(false))
    .backend(new Backend(_))
    .render { (P, S, B) =>

      g({
        import japgolly.scalajs.react.vdom.all._
        onClick --> B.select
      })(rect(width := "200", height := "40", fill := {
        if (S.selected) {
          GlobalStyles.textColor
        } else {
          GlobalStyles.leftNavBackgrounColor
        }
      }, x := 20, y := (P.index * 45) + 30, stroke := GlobalStyles.textColor),

        text(x := 30, y := (P.index * 45) + 60, fill := {
          if (S.selected) {
            GlobalStyles.leftNavBackgrounColor
          } else {
            GlobalStyles.textColor
          }
        }, fontSize := "18px")(P.dep.tpe.name)
      )
    }.build

  def apply(dep: RoleDependency, index: Int, selectDep: (RoleDependency, Boolean) => Unit) = component(Props(dep, index, selectDep))
}

object GraphNode {

  case class Props(node: ClusterGraphNode, force: ForceLayout)

  case class State(selected: Boolean)

  class Backend(t: BackendScope[Props, State]) {
    def select = t.modState(_.copy(selected = !t.state.selected))
  }

  val component = ReactComponentB[Props]("GraphNode")
    .initialStateP(P => State(false))
    .backend(new Backend(_))
    .render { (P, S, B) =>
      g(
        circle(Attrs.cls := "node", Attrs.id := P.node.index, r := "30", cx := P.node.x, cy := P.node.y,
          fill := {

            if (S.selected) {
              "#EEE"
            } else {
              P.node.status match {
                case "Up" => GlobalStyles.nodeUpColor
                case "Unreachable" => GlobalStyles.nodeUnreachableColor
                case "Removed" => GlobalStyles.nodeRemovedColor
                case "Exited" => GlobalStyles.nodeRemovedColor
              }

            }

          }, stroke := "#fff", strokeWidth := "1.px5"
        //        , {
        //            import japgolly.scalajs.react.vdom.all._
        //            onClick --> B.select
        //
        //          }
        ),
        text(x := P.node.x - 30, y := P.node.y - 55, fill := "white", fontSize := "18px")(P.node.host),
        text(x := P.node.x - 30, y := P.node.y - 35, fill := "#D5EFD5", fontSize := "18px")(P.node.roles)
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

  case class Props(link: ClusterGraphLink, key: Int, mode: Mode)

  val component = ReactComponentB[Props]("GraphLink")
    .render { P =>

      P.mode match {
        case Members =>
          line(
            Attrs.cls := "link",
            x1 := P.link.source.x,
            y1 := P.link.source.y,
            x2 := P.link.target.x,
            y2 := P.link.target.y,
            stroke := "#999",
            strokeOpacity := ".6",
            strokeWidth := "1",
            strokeDasharray := "5,5")
        case Roles =>
          line(
            Attrs.cls := "link",
            x1 := P.link.source.x,
            y1 := P.link.source.y,
            x2 := P.link.target.x,
            y2 := P.link.target.y,
            stroke := "#999",
            strokeOpacity := "1",
            strokeWidth := "3")
        case Nodes =>
          line(
            Attrs.cls := "link",
            x1 := P.link.source.x,
            y1 := P.link.source.y,
            x2 := P.link.target.x,
            y2 := P.link.target.y,
            stroke := "#999",
            strokeOpacity := "1",
            strokeWidth := "1")

      }

    }.build

  def apply(link: ClusterGraphLink, key: Int, mode: Mode) = component(Props(link, key, mode))
}

object Graph {

  import clusterconsole.client.style.CustomTags._

  case class Props(system: String, mode: Mode, width: Double, height: Double,
    store: ClusterStore, fixedMap: Boolean)

  case class State(nodes: Seq[ClusterGraphNode],
    links: Seq[ClusterGraphLink],
    force: ForceLayout)

  def drawLinks(links: Seq[ClusterGraphLink], mode: Mode): ReactNode =
    g(links.zipWithIndex.map { case (eachLink, i) => GraphLink(eachLink, i, mode) })

  def drawNodes(nodes: Seq[ClusterGraphNode], force: ForceLayout): Seq[ReactNode] =
    nodes.zipWithIndex.map {
      case (node, i) =>
        GraphNode(node, force)
    }

  def drawDeps(roles: Seq[RoleDependency], select: (RoleDependency, Boolean) => Unit): Seq[ReactNode] =
    roles.zipWithIndex.map {
      case (dep, i) => ClusterDependencyLegend(dep, i, select)
    }

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      react(t.props.store.getSelectedCluster, updateGraph)
      react(t.props.store.getSelectedDeps, updateLinkDeps)
    }

    def selectDep(rd: RoleDependency, selected: Boolean) = {

      log.debug("selectDep " + rd.tpe.name + " " + selected)

      t.props.store.getSelectedCluster().foreach(cluster =>
        ClusterStoreActions.selectRoleDependency(cluster.system, rd, selected)
      )

    }

    def updateLinkDeps(c: Map[String, List[RoleDependency]]) = {
      t.props.store.getSelectedCluster().foreach(cluster =>
        c.get(cluster.system).foreach(deps =>
          t.modState { s =>
            val links: Seq[ClusterGraphLink] = getLinks(t.state.nodes, t.props.mode, cluster, deps)
            val nodeUpdateState = s.copy(nodes = t.state.nodes, force = s.force.nodes(t.state.nodes.toJsArray).start())
            nodeUpdateState.copy(links = links)
            s.copy(links = links)
          }
        )
      )

    }

    def updateGraph(c: Option[DiscoveredCluster]) = {

      c.fold[Unit]({})(cluster => {

        log.debug("updateGraph")

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
            )(cn => {
                val fixedList = t.props.store.getDiscoveredClusterNodes().getOrElse(cluster.system, Nil)
                fixedList.find(_.host == node.address.label).fold(
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

                )(fixedNode => {
                    log.debug("^^^^^^^^^^^^^  fixedNode  " + fixedNode.x)

                    js.Dynamic.literal(
                      "host" -> node.address.label,
                      "roles" -> node.roles.mkString(","),
                      "status" -> node.state.toString,
                      "name" -> node.label,
                      "index" -> fixedNode.index,
                      "x" -> fixedNode.x,
                      "y" -> fixedNode.y,
                      "px" -> fixedNode.px,
                      "py" -> fixedNode.py,
                      "fixed" -> fixedNode.fixed,
                      "weight" -> cn.weight
                    ).asInstanceOf[ClusterGraphNode]
                  })
              })
          }

        t.modState { s =>
          val links: Seq[ClusterGraphLink] = {

            t.props.mode match {
              case Roles =>
                getLinks(incomingNodes, t.props.mode, cluster, t.props.store.getSelectedDeps().getOrElse(cluster.system, Nil))

              case _ => getLinks(incomingNodes, t.props.mode, cluster)
            }
          }
          val nodesToForce = incomingNodes.filter(_.fixed == false)
          val nodeUpdateState = s.copy(nodes = incomingNodes, force = s.force.nodes(nodesToForce.toJsArray).start())
          nodeUpdateState.copy(links = links)
        }
      })
    }

    def tick() = {
      val newNodes: List[ClusterGraphNode] = t.state.force.nodes().toList
      val notFixed = newNodes.filter(_.fixed == false)
      val fixed = t.state.nodes.filter(_.fixed == true)
      t.modState(s => s.copy(nodes = notFixed ++ fixed))
    }

    def start() = {
      t.modState { s =>

        val nodesToForce = t.state.nodes.filter(_.fixed == false)

        val firstState = s.copy(force = s.force.nodes(nodesToForce.toJsArray).start())
        firstState.copy(force = s.force.on("tick", () => tick))
      }
    }

    def resume() = {
      t.modState { s =>
        val firstState = s.copy(force = s.force.nodes(t.state.nodes.toJsArray).resume())
        firstState.copy(force = s.force.on("tick", () => tick))
      }

    }

    def dragEnd(a: js.Any, b: Double): js.Any = {
      t.state.nodes.find(_.index == b).foreach(node =>
        ClusterStoreActions.updateClusterNode(t.props.system, node)
      )
    }

    def dragMove(a: js.Any, b: Double): js.Any = {

      t.props.store.getSelectedCluster().foreach(cluster => {
        val mouse = d3.mouse(js.Dynamic.global.document.getElementById(b.toString))

        val newNodes: Seq[ClusterGraphNode] = t.state.nodes.map(n =>
          if (n.index == b) {
            js.Dynamic.literal(
              "host" -> n.host,
              "roles" -> n.roles,
              "status" -> n.status,
              "name" -> n.name,
              "index" -> b,
              "x" -> mouse(0),
              "y" -> mouse(1),
              "px" -> n.px,
              "py" -> n.py,
              "fixed" -> true,
              "weight" -> n.weight
            ).asInstanceOf[ClusterGraphNode]

          } else {
            n
          }
        )

        val newLinks: Seq[ClusterGraphLink] = getLinks(newNodes, t.props.mode, cluster)

        t.modState(s => s.copy(nodes = newNodes, links = newLinks))
      }
      )

      //      t.modState(s => s.copy(nodes = newNodes))

    }

  }

  val component = ReactComponentB[Props]("Graph")
    .initialStateP { P =>

      val force = d3.layout.force()
        .size(List[Double](P.width, P.height).toJsArray)
        .charge(-1500)
        .linkDistance(500)
      //        .chargeDistance(1000)
      //        .linkDistance(500)
      //        .friction(0.9)

      val (nodes, links) = P.store.getSelectedCluster().map(cluster => {
        getNodesAndLink(cluster,
          P.mode,
          P.store.getDiscoveredClusterNodes().getOrElse(cluster.system, Nil))
      }).getOrElse((Nil, Nil))

      State(nodes, links, force)

    }.backend(new Backend(_))
    .render((P, S, B) => {

      val roles: Seq[RoleDependency] =
        if (P.mode == Roles) {
          P.store.getSelectedCluster().map(_.dependencies).getOrElse(Nil)
        } else {
          Nil
        }

      svgtag(SvgAttrs.width := P.width, SvgAttrs.height := P.height)(
        drawDeps(roles, B.selectDep),
        drawLinks(S.links, P.mode),
        drawNodes(S.nodes, S.force)
      )
    }).componentWillReceiveProps { (scope, P) =>
      val (nodes, links) = P.store.getSelectedCluster().map(cluster => {
        getNodesAndLink(cluster, P.mode,
          P.store.getDiscoveredClusterNodes().getOrElse(cluster.system, Nil))
      }).getOrElse((Nil, Nil))

      val newState = State(nodes, links, scope.state.force)

      scope.modState { s =>
        val firstState = s.copy(nodes = nodes, links = links, force = s.force.nodes(nodes.toJsArray).start())
        firstState.copy(force = s.force.on("tick", () => scope.backend.tick))
      }

    }.componentWillMount { scope =>
      if (!scope.props.fixedMap) {
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
      scope.state.force.stop()
    }.configure(OnUnmount.install).build

  def apply(system: String, mode: Mode, width: Double, height: Double, store: ClusterStore, fixedMap: Boolean) = {

    component(Props(system, mode, width, height, store, fixedMap))
  }

  def getNodesAndLink(cluster: DiscoveredCluster,
    mode: Mode,
    fixedList: List[ClusterGraphNode]): (Seq[ClusterGraphNode], Seq[ClusterGraphLink]) = {
    val nodes = cluster.members.toSeq.zipWithIndex.map {
      case (node, i) =>

        fixedList.find(_.host == node.address.label).fold(
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
        )(fixedNode => {

            log.debug("getNodesAndLink " + fixedNode.x)
            js.Dynamic.literal(
              "host" -> fixedNode.host,
              "roles" -> fixedNode.roles,
              "status" -> node.state.toString,
              "name" -> node.label,
              "index" -> fixedNode.index,
              "x" -> fixedNode.x,
              "y" -> fixedNode.y,
              "px" -> fixedNode.px,
              "py" -> fixedNode.py,
              "fixed" -> fixedNode.fixed,
              "weight" -> fixedNode.weight
            ).asInstanceOf[ClusterGraphNode]

          }

          )
    }
    (nodes, getLinks(nodes, mode, cluster))

  }

  def getNewIndex(set: Set[Double], v: Double): Double =
    if (set.contains(v)) {
      getNewIndex(set, v + 1)
    } else {
      v
    }

  def getLinks(nodes: Seq[ClusterGraphNode], mode: Mode, cluster: DiscoveredCluster, roleDependencies: List[RoleDependency] = Nil) = {

    val connections: Seq[(Double, Double)] = mode match {
      case Members =>
        Seq.empty[(Double, Double)]
        val indexes = nodes.filter(_.status == "Up").map(_.index)
        //        indexes.foldLeft(Seq.empty[(Double, Double)])((a, b) =>
        //
        //          if (a.isEmpty) {
        //            indexes match {
        //              case Nil => Nil
        //              case h :: Nil => Nil
        //              case list => Seq((list.reverse.head, list.head))
        //            }
        //          } else {
        //            a :+ (a.reverse.head._2, b)
        //          }
        //
        //        )
        indexes.flatMap(index => indexes.filter(_ > index).map((index, _)))
      case Roles =>

        log.debug("getLinks = " + roleDependencies)

        roleDependencies.flatMap { rd =>

          val sourcesIndexes = rd.roles.flatMap { eachRole =>
            cluster.getNodesByRole(eachRole).toSeq.flatMap(e =>
              nodes.filter(_.host == e.address.label).map(_.index))
          }

          val targetsIndexes = rd.dependsOn.flatMap { eachRole =>
            cluster.getNodesByRole(eachRole).toSeq.flatMap(e =>
              nodes.filter(_.host == e.address.label).map(_.index))
          }

          sourcesIndexes.flatMap(eachSource =>
            targetsIndexes.map(eachTarget =>
              (eachSource, eachTarget)))
        }

      case Nodes => Seq.empty[(Double, Double)]
    }

    connections.flatMap {
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

  }

}

