package clusterconsole.client.modules

import clusterconsole.client.ClusterConsoleApp.Loc
import clusterconsole.client.components._
import clusterconsole.client.services.Logger._
import clusterconsole.client.services.{ ActivityLogService, ClusterStore, ClusterStoreActions }
import clusterconsole.client.style.Bootstrap.Button
import clusterconsole.client.style.{ GlobalStyles, Icon }
import clusterconsole.http.{ HostPortUtil, HostPort, ClusterForm, DiscoveredCluster }
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.all._
import rx._
import scalacss.ScalaCssReact._

sealed trait Mode {
  def isActive(m: Mode): Boolean = this == m
}
object Mode {
  def fromString(s: String) =
    s match {
      case "Members" => Members
      case "Roles" => Roles
      case "Nodes" => Nodes
    }
}
case object Members extends Mode
case object Roles extends Mode
case object Nodes extends Mode

object ClusterMap {

  @inline private def globalStyles = GlobalStyles

  case class Props(store: ClusterStore, router: RouterCtl[Loc])

  case class State(showClusterForm: Boolean, mode: Mode)

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      ClusterStoreActions.getDiscoveredClusters()
    }

    def editCluster(item: ClusterForm): Unit = {
      log.debug("item " + item)
      ClusterStoreActions.subscribeToCluster(item.name, item.seeds.map(HostPortUtil.apply))
    }

    def selectCluster(name: String) = {
      ClusterStoreActions.selectCluster(name)
    }

    def showClusterForm(show: Boolean) = {
      t.modState(_.copy(showClusterForm = show))
    }

    def closeClusterForm = showClusterForm(false)

    def changeMode(e: ReactMouseEvent) = {
      t.modState(_.copy(mode = Mode.fromString(e.currentTarget.childNodes.item(0).childNodes.item(0).childNodes.item(0).nodeValue)))
      e.preventDefault()
    }

  }

  // create the React component for Clusters mgmt
  val component = ReactComponentB[Props]("Clusters")
    .initialState(State(false, Members)) // initial state from TodoStore
    .backend(new Backend(_))
    .render((P, S, B) => {

      val toolBar: ReactElement =
        div(cls := "row", globalStyles.mainHeaders)(
          div(cls := "col-md-8")(h3("Clusters")),
          div(cls := "col-md-4")(button(cls := "pull-right btn-lg", marginTop := "9px",
            tpe := "button", onClick --> B.showClusterForm(true))(Icon.plus)))

      val modal: Seq[ReactElement] = if (S.showClusterForm) Seq(ClusterFormComponent(P.store, B.editCluster,
        () => B.closeClusterForm))
      else Seq.empty[ReactElement]

      val leftNav: Seq[ReactElement] = toolBar +: (Seq(DiscoveringClusterComponent(P.store.getDiscoveringClusters),
        DiscoveredClusterComponent(P.store.getDiscoveredClusters, P.store.getSelectedCluster, S.mode),
        ActivityLogComponent(ActivityLogService)
      ) ++ modal)

      def renderModeButton(m: Mode) =
        if (S.mode.isActive(m)) {
          li(cls := "active", onClick ==> B.changeMode, globalStyles.mainHeaders, borderTop := "1px solid white")(
            a(href := "", globalStyles.mainHeaders, backgroundColor := globalStyles.mapBackground)(
              span(color := globalStyles.textColor)(fontSize := "24px")(m.toString)
            )
          )

        } else {
          li(onClick ==> B.changeMode, globalStyles.mainHeaders)(
            a(href := "", globalStyles.mainHeaders)(
              span(fontSize := "24px")(m.toString)
            )
          )

        }
      val clusterMapToolBar =
        div(cls := "row", globalStyles.mainHeaders)(
          ul(cls := "nav nav-tabs")(
            renderModeButton(Members),
            renderModeButton(Roles),
            renderModeButton(Nodes)
          )
        )

      div(cls := "row")(
        div(cls := "col-md-3", globalStyles.leftNav, height := "100%")(
          leftNav
        ),
        div(cls := "col-md-9")(
          clusterMapToolBar,
          div(cls := "row")(
            div(cls := "col-md-12")(
              ClusterNodeGraphComponent(P.store, S.mode)
            )
          )
        )
      )
    })
    .componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  /** Returns a function with router location system while using our own props */
  def apply(store: ClusterStore) = (router: RouterCtl[Loc]) => {
    component(Props(store, router))
  }

}
