package clusterconsole.client.components.graph

import clusterconsole.client.d3.Layout._
import clusterconsole.client.d3._
import clusterconsole.client.domain._
import clusterconsole.client.modules._
import clusterconsole.client.services.ClusterService
import clusterconsole.client.services.Logger._
import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{ ClusterMember, DiscoveredCluster, RoleDependency }
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.SvgAttrs
import japgolly.scalajs.react.vdom.all.svg._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ ReactComponentB, ReactNode, _ }

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object Graph {

  import clusterconsole.client.style.CustomTags._

  case class Props(system: String, mode: Mode, width: Double, height: Double,
    store: ClusterService, fixedMap: Boolean)

  case class State(nodes: Seq[ClusterGraphNode],
    links: Seq[ClusterGraphLink],
    force: ForceLayout)

  def drawLinks(links: Seq[ClusterGraphLink], mode: Mode): ReactNode =
    g(links.zipWithIndex.map { case (eachLink, i) => GraphLink(eachLink, i, mode) })

  def drawNodes(nodes: Seq[ClusterGraphNode], force: ForceLayout, mode: Mode): Seq[ReactNode] =
    nodes.zipWithIndex.map {
      case (node, i) =>
        GraphNode(node, force, mode)
    }

  def drawDeps(roles: Seq[(RoleDependency, Boolean)], select: (RoleDependency, Boolean) => Unit): Seq[ReactNode] =
    roles.zipWithIndex.map {
      case ((dep, selected), i) => ClusterDependencyLegend(dep, i, selected, select)
    }

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      react(t.props.store.getSelectedCluster, updateGraph)
      react(t.props.store.getSelectedDeps, updateLinkDeps)
    }

    def selectDep(rd: RoleDependency, selected: Boolean) = {

      log.debug("selectDep " + rd.tpe.name + " " + selected)

      t.props.store.getSelectedCluster().foreach(cluster =>
        ClusterService.selectRoleDependency(cluster.system, rd, selected)
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

    def getFixedList(system: String): List[ClusterGraphNode] =
      t.props.store.getFixedNodePositions()
        .getOrElse(system, Map.empty[Mode, List[ClusterGraphNode]])
        .getOrElse(t.props.mode, Nil)

    def fixedHostPosition(system: String, host: String, cn: ClusterGraphNode) =
      getFixedList(system).find(e => e.host == host && e.port == 0).fold(
        ClusterGraphNode.host(host, cn.index, cn.x, cn.y, cn.px, cn.py, cn.fixed, cn.weight)
      )(fixedNode => {
          js.Dynamic.literal(
            "host" -> cn.host,
            "port" -> cn.port,
            "roles" -> cn.roles,
            "status" -> "Up",
            "name" -> cn.name,
            "index" -> cn.index,
            "x" -> cn.x,
            "y" -> cn.y,
            "px" -> cn.px,
            "py" -> cn.py,
            "fixed" -> true,
            "weight" -> cn.weight
          ).asInstanceOf[ClusterGraphNode]
        })

    /**
     * ClusterMember => ClusterGraphNode, checking if this has a fixed position from dragging
     */
    def fixNodePosition(system: String, member: ClusterMember, cn: ClusterGraphNode)(implicit ev: MemberLike[ClusterGraphNode, ClusterMember]) =

      getFixedList(system).find(e => ev.nodeEq(e, member)).fold(
        ClusterGraphNode(member, cn.index, cn.x, cn.y, cn.px, cn.py, cn.fixed, cn.weight)
      )(fixedNode => {
          js.Dynamic.literal(
            "host" -> cn.host,
            "port" -> cn.port,
            "roles" -> cn.roles,
            "status" -> member.state.toString,
            "name" -> cn.name,
            "index" -> cn.index,
            "x" -> cn.x,
            "y" -> cn.y,
            "px" -> cn.px,
            "py" -> cn.py,
            "fixed" -> true,
            "weight" -> cn.weight
          ).asInstanceOf[ClusterGraphNode]
        })

    def updateGraph(c: Option[DiscoveredCluster]) = {

      c.fold[Unit]({})(cluster => {

        log.debug("updateGraph")

        val existingIndexes = t.state.nodes.map(_.index).toSet

        val incomingNodes: Seq[ClusterGraphNode] =
          t.props.mode match {
            case Nodes =>

              // get a node map of what is currently on screen
              val currentNodesMap = t.state.nodes.map(e => (ClusterGraphNode.label(e), e)).toMap

              log.debug("currentNodesMap " + currentNodesMap.toList.map(e => (e._1, e._2.host + ":" + e._2.port)))

              // add host nodes in
              val hostMap = cluster.members.toSeq.groupBy(m => m.address.host)

              // this is actual cluster state from server, nodes could have been added there,
              // must be added here. check existence in current map, if not there, add one, else
              // check fixed position
              val ports = cluster.members.toSeq.map { node =>
                currentNodesMap.get(node.address.label).fold(
                  ClusterGraphNode(node, getNewIndex(existingIndexes, 1), 450, 450, 450, 450, false, 0)
                )(cn => fixNodePosition(t.props.system, node, cn))
              }

              val hosts = hostMap.keys.toSeq.map(hostName =>
                currentNodesMap.get(hostName + ":0").fold(
                  ClusterGraphNode.host(hostName, getNewIndex(existingIndexes, 1), 450, 450, 450, 450, false, 0)
                )(cn => fixedHostPosition(t.props.system, hostName, cn))

              )

              hosts ++: ports

            case _ =>

              // get a node map of what is currently on screen
              val currentNodesMap = t.state.nodes.map(e => (ClusterGraphNode.label(e), e)).toMap

              // this is actual cluster state from server, nodes could have been added there,
              // must be added here. check existence in current map, if not there, add one, else
              // check fixed position
              val res = cluster.members.toSeq.map { node =>
                currentNodesMap.get(node.address.label).fold(
                  ClusterGraphNode(node, getNewIndex(existingIndexes, 1), 450, 450, 450, 450, false, 0)
                )(cn => fixNodePosition(t.props.system, node, cn))
              }
              res

          }

        log.debug("********** incomingNodes = " + incomingNodes.map(e => e.port + " " + e.index))

        log.debug("********** cluster deps = " + cluster.dependencies)

        t.modState { s =>
          val links: Seq[ClusterGraphLink] = getLinks(incomingNodes, t.props.mode, cluster, t.props.store.getSelectedDeps().getOrElse(cluster.system, Nil))
          s.copy(nodes = incomingNodes, links = links, force = s.force.nodes(incomingNodes.toJsArray).start())
        }
      })
      initDrag()
    }

    def renderTick() = {
      val newNodes: List[ClusterGraphNode] = t.state.force.nodes().toList
      //      val notFixed = newNodes.filter(_.fixed == false)
      //      val fixed = t.state.nodes.filter(_.fixed == true)
      t.modState(s => s.copy(nodes = newNodes))
    }

    def startfixed() = {
      t.modState { s =>
        val firstState = s.copy(force = s.force.nodes(t.state.nodes.toJsArray).start())
        (1 until 150).foreach(i => t.state.force.tick())
        firstState.copy(force = s.force.on("tick", () => renderTick))
      }
    }

    def initDrag(): Unit = {
      val drag = t.state.force.drag().on("dragend", (a: js.Any, b: Double) => dragEnd[ClusterGraphNode](a, b))
      d3.select("svg").
        selectAll(".node").
        data(t.state.nodes.toJSArray).
        call(drag)

    }

    def dragEnd[T: NodeLike](d: js.Any, x: Double) = {

      val node = d.asInstanceOf[ClusterGraphNode]

      t.modState { s =>
        val newNodes =
          s.nodes.map { e =>
            if (implicitly[NodeLike[ClusterGraphNode]].nodeEq(e, node)) {
              js.Dynamic.literal(
                "virtualHost" -> e.name,
                "host" -> e.host,
                "port" -> e.port,
                "roles" -> e.roles,
                "status" -> e.status,
                "name" -> e.name,
                "index" -> e.index,
                "x" -> e.x,
                "y" -> e.y,
                "px" -> e.px,
                "py" -> e.py,
                "fixed" -> true,
                "weight" -> e.weight
              ).asInstanceOf[ClusterGraphNode]
            } else {
              e
            }
          }
        s.copy(nodes = newNodes, force = s.force.nodes(newNodes.toJSArray).start())
      }

      t.state.nodes.find(e => implicitly[NodeLike[ClusterGraphNode]].nodeEq(e, node)).foreach { node =>

        log.debug("ClusterService.updateNodePosition node: " + node.host + ":" + node.port)

        ClusterService.updateNodePosition(t.props.system, t.props.mode, node)
      }

      updateGraph(t.props.store.getSelectedCluster())

      //      initDrag()
    }
  }

  val component = ReactComponentB[Props]("Graph")
    .initialStateP { P =>

      val force = d3.layout.force()
        .size(List[Double](P.width, P.height).toJsArray)
        .charge(-1500)
        .linkDistance(1000)
        .friction(0.9)

      val (nodes, links) = P.store.getSelectedCluster().map(cluster => {
        getNodesAndLink(cluster,
          P.mode,
          P.store.getFixedNodePositions().getOrElse(cluster.system, Map.empty[Mode, List[ClusterGraphNode]]).getOrElse(P.mode, Nil),
          P.store.getSelectedDeps().getOrElse(cluster.system, Nil))
      }).getOrElse((Nil, Nil))

      State(nodes, links, force)

    }.backend(new Backend(_))
    .render((P, S, B) => {

      val selectedDeps = P.store.getSelectedDeps().getOrElse(P.system, Nil)

      val roles: Seq[(RoleDependency, Boolean)] =
        if (P.mode == Roles) {
          P.store.getSelectedCluster().map(_.dependencies).getOrElse(Nil)
            .map(eachDep => (eachDep, selectedDeps.exists(_.tpe.name == eachDep.tpe.name)))
        } else {
          Nil
        }

      svgtag(SvgAttrs.width := P.width, SvgAttrs.height := P.height)(
        drawDeps(roles, B.selectDep),
        drawLinks(S.links, P.mode),
        drawNodes(S.nodes, S.force, P.mode)
      )
    }).componentWillReceiveProps { (scope, P) =>
      val (nodes, links) = P.store.getSelectedCluster().map(cluster => {
        getNodesAndLink(cluster, P.mode,
          P.store.getFixedNodePositions().getOrElse(cluster.system, Map.empty[Mode, List[ClusterGraphNode]]).getOrElse(P.mode, Nil),
          P.store.getSelectedDeps().getOrElse(cluster.system, Nil))
      }).getOrElse((Nil, Nil))

      val newState = State(nodes, links, scope.state.force)

      scope.modState { s =>
        val firstState = s.copy(nodes = nodes, links = links, force = s.force.nodes(nodes.toJsArray).start())
        firstState.copy(force = s.force.on("tick", () => scope.backend.renderTick))
      }

    }.componentWillMount { scope =>
      if (!scope.props.fixedMap) {
        scope.backend.startfixed()
      }

    }.componentDidMount { scope =>
      scope.backend.mounted()

      scope.backend.initDrag()

    }.componentWillUnmount { scope =>
      scope.state.force.stop()
    }.configure(OnUnmount.install).build

  def apply(system: String, mode: Mode, width: Double, height: Double, store: ClusterService, fixedMap: Boolean) = {

    component(Props(system, mode, width, height, store, fixedMap))
  }

  def getNodesAndLink(cluster: DiscoveredCluster,
    mode: Mode,
    fixedList: List[ClusterGraphNode],
    selectedDeps: List[RoleDependency])(implicit ev: MemberLike[ClusterGraphNode, ClusterMember]): (Seq[ClusterGraphNode], Seq[ClusterGraphLink]) = {
    val nodes: Seq[ClusterGraphNode] =

      mode match {
        case Nodes =>

          // group by host
          val map = cluster.members.toSeq.groupBy(m => m.address.host)

          var newKeyIndex = 1000

          map.keys.toSeq.zipWithIndex.flatMap {
            case (key, keyIndex) =>

              val ports: Seq[ClusterMember] = map.getOrElse[Seq[ClusterMember]](key, Seq.empty[ClusterMember])

              val portNodes: Seq[ClusterGraphNode] = ports.zipWithIndex.map {
                case (pNode, pIndex) =>
                  fixedList.find(e => ev.nodeEq(e, pNode)).fold(
                    ClusterGraphNode.port(pNode, newKeyIndex + keyIndex + 1 + pIndex, 450, 450, 450, 450, false, 1)
                  )(found =>
                      ClusterGraphNode.port(pNode, newKeyIndex + keyIndex + 1 + pIndex, found.x, found.y, found.px, found.py, true, 1)
                    )

              }

              val hostNode: Option[ClusterGraphNode] =
                ports.headOption.map(firstPort =>
                  fixedList.find(e => e.host == firstPort.address.host && e.port == 0).fold(
                    ClusterGraphNode.host(firstPort.address.host, newKeyIndex + keyIndex, 450, 450, 450, 450, false, ports.length)
                  )(found =>
                      ClusterGraphNode.host(firstPort.address.host, newKeyIndex + keyIndex, found.x, found.y, found.px, found.py, true, ports.length)
                    ))

              val res = hostNode.fold(Seq.empty[ClusterGraphNode])(hn => hn +: portNodes)

              newKeyIndex = newKeyIndex + ports.length + 1

              res
          }

        case _ =>
          cluster.members.toSeq.zipWithIndex.map {
            case (node, i) =>
              fixedList.find(e => ev.nodeEq(e, node)).fold(
                ClusterGraphNode(node, i, 450, 450, 450, 450, false, 0)
              )(fixedNode => {
                  ClusterGraphNode(node,
                    fixedNode.index, fixedNode.x, fixedNode.y, fixedNode.px, fixedNode.py, fixedNode.fixed, 0)
                })
          }
      }
    (nodes.toSeq, getLinks(nodes.toSeq, mode, cluster, selectedDeps))
  }

  def getNewIndex(set: Set[Double], v: Double): Double =
    if (set.contains(v)) {
      getNewIndex(set, v + 1)
    } else {
      v
    }

  def getLinks(nodes: Seq[ClusterGraphNode],
    mode: Mode,
    cluster: DiscoveredCluster,
    roleDependencies: List[RoleDependency] = Nil)(implicit ev: MemberLike[ClusterGraphNode, ClusterMember]) = {

    def makeLinks(conns: Seq[(Double, Double)]) =
      conns.flatMap {
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

    mode match {
      case Members =>
        val indexes = nodes.filter(_.status == "Up").map(_.index)
        makeLinks(indexes.flatMap(index => indexes.filter(_ > index).map((index, _))))

      case Roles =>
        log.debug("getLinks = " + roleDependencies)
        val allDeps = cluster.dependencies

        log.debug("allDeps: " + allDeps)

        roleDependencies.zipWithIndex.flatMap {
          case (rd, index) =>

            log.debug("--------- roleDependencies " + (rd, index))

            val sourcesIndexes = rd.roles.flatMap { eachRole =>
              cluster.getNodesByRole(eachRole).toSeq.flatMap(e =>
                nodes.filter(n => ev.nodeEq(n, e)).map(_.index))
            }

            val targetsIndexes = rd.dependsOn.flatMap { eachRole =>
              cluster.getNodesByRole(eachRole).toSeq.flatMap(e =>
                nodes.filter(n => ev.nodeEq(n, e)).map(_.index))
            }

            val indexes = sourcesIndexes.flatMap(eachSource =>
              targetsIndexes.map(eachTarget =>
                (eachSource, eachTarget)))

            // get index of RoleDep

            indexes.flatMap {
              case (a, b) =>
                for {
                  source <- nodes.find(_.index == a)
                  target <- nodes.find(_.index == b)
                } yield {

                  log.debug("****************  rd " + rd + " " + allDeps.indexOf(rd))

                  js.Dynamic.literal(
                    "index" -> allDeps.indexOf(rd),
                    "source" -> source,
                    "target" -> target,
                    "sourceHost" -> source.host,
                    "targetHost" -> target.host).asInstanceOf[ClusterGraphRoleLink]
                }
            }

        }

      case Nodes =>

        //join ports to hosts
        val hostPortMap: Map[String, Seq[ClusterGraphNode]] = nodes.groupBy(n => n.host)

        log.debug("hostPortMap " + hostPortMap)
        log.debug("hostPortMap " + hostPortMap.size)

        val hostToPortIndexes = hostPortMap.foldLeft[Seq[(Double, Double)]](Seq.empty[(Double, Double)])((a, b) => a ++ {

          log.debug("-- b._1 " + b._1)
          log.debug("-- b._2 " + b._2.map(p => p.port + " " + p.index))

          nodes.find(e => e.host == b._1 && e.port == 0).map(host =>
            b._2.flatMap(e => if (e.port != 0) {
              Some((host.index, e.index))
            } else None)).getOrElse(Nil)
        }
        )
        makeLinks(hostToPortIndexes)
    }

  }

}

