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

object ClusterMap {

  @inline private def bss = GlobalStyles

  case class Props(store: ClusterStore, router: RouterCtl[Loc])

  case class State(showClusterForm: Boolean)

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

    def showClusterForm = {
      t.modState(_.copy(showClusterForm = true))
    }

  }

  // create the React component for Clusters mgmt
  val component = ReactComponentB[Props]("Clusters")
    .initialState(State(false)) // initial state from TodoStore
    .backend(new Backend(_))
    .render((P, S, B) => {

      val toolBar: ReactElement =
        div(cls := "row")(
          div(cls := "col-md-8")(h3("Clusters")),
          div(cls := "col-md-4")(button(cls := "pull-right btn-default",
            tpe := "button", onClick --> B.showClusterForm)(Icon.plus, " Discover")))

      val modal: Seq[ReactElement] = if (S.showClusterForm) Seq(ClusterFormComponent(P.store, B.editCluster)) else Seq.empty[ReactElement]

      val items: Seq[ReactElement] = toolBar +: (Seq(DiscoveringClusterComponent(P.store.getDiscoveringClusters),
        DiscoveredClusterComponent(P.store.getDiscoveredClusters, P.store.getSelectedCluster),
        ActivityLogComponent(ActivityLogService)
      ) ++ modal)

      div(cls := "row")(
        div(cls := "col-md-3", bss.leftNav, height := "100%")(items),
        div(cls := "col-md-9")(
          h3("Cluster map"),
          ClusterNodeGraphComponent(P.store)
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
