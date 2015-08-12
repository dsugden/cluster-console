package clusterconsole.client.components.graph

import clusterconsole.client.d3.Layout.ForceLayout
import clusterconsole.client.domain.ClusterGraphNode
import clusterconsole.client.modules.{ Mode, Nodes }
import clusterconsole.client.style.GlobalStyles
import japgolly.scalajs.react.vdom.Attrs
import japgolly.scalajs.react.vdom.all.svg._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ ReactComponentB, ReactNode, _ }

object GraphNode {

  case class Props(node: ClusterGraphNode, force: ForceLayout, mode: Mode)

  case class State(selected: Boolean)

  class Backend(t: BackendScope[Props, State]) {
    def select = t.modState(_.copy(selected = !t.state.selected))
  }

  val component = ReactComponentB[Props]("GraphNode")
    .initialStateP(P => State(false))
    .backend(new Backend(_))
    .render { (P, S, B) =>
      g(
        circle(Attrs.cls := "node", Attrs.id := P.node.index, r := getRadius(P.mode, P.node), cx := P.node.x, cy := P.node.y,
          fill := {

            if (S.selected) {
              "#EEE"
            } else {
              P.node.status match {
                case "Up" => GlobalStyles.nodeUpColor
                case "Unreachable" => GlobalStyles.nodeUnreachableColor
                case "Removed" => GlobalStyles.nodeRemovedColor
                case "Exited" => GlobalStyles.nodeRemovedColor
                case _ => GlobalStyles.nodeUpColor
              }
            }
          }, stroke := "#fff", strokeWidth := "1.px5"
        //        , {
        //            import japgolly.scalajs.react.vdom.all._
        //            onClick --> B.select
        //
        //          }
        ),
        getTextNodes(P.mode, P.node)
      )

    }.build

  def getRadius(mode: Mode, n: ClusterGraphNode): String = mode match {
    case Nodes =>
      if (n.port == 0) {
        "50"
      } else {
        "20"
      }
    case _ => "30"
  }

  def getTextNodes(mode: Mode, n: ClusterGraphNode): ReactNode = mode match {
    case Nodes =>
      if (n.port == 0) {
        g(
          text(x := n.x - 40, y := n.y - 55, fill := "white", fontSize := "18px")(n.host)
        )

      } else {
        g(
          text(x := n.x - 30, y := n.y - 55, fill := "white", fontSize := "18px")(n.port),
          text(x := n.x - 30, y := n.y - 35, fill := "#D5EFD5", fontSize := "18px")(n.roles)
        )
      }
    case _ => g(
      text(x := n.x - 30, y := n.y - 55, fill := "white", fontSize := "18px")(n.host + ":" + n.port),
      text(x := n.x - 30, y := n.y - 35, fill := "#D5EFD5", fontSize := "18px")(n.roles)
    )
  }

  def apply(node: ClusterGraphNode, force: ForceLayout, mode: Mode) = component(Props(node, force, mode))
}