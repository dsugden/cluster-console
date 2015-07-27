package clusterconsole.client.modules

import clusterconsole.client.ClusterConsoleApp.Loc
import clusterconsole.client.components._
import clusterconsole.client.services.Logger._
import clusterconsole.client.services.{ ActivityLogService, ClusterStore, ClusterStoreActions }
import clusterconsole.http.{ HostPortUtil, HostPort, ClusterForm, DiscoveredCluster }
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.all._
import rx._

object ClusterMap {

  case class Props(store: ClusterStore, router: RouterCtl[Loc])

  case class State()

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      observe(t.props.store.getDiscoveredClusters)
      observe(t.props.store.getDiscoveringClusters)
      observe(t.props.store.getSelectedCluster)
      //      observe(t.state.selectedItem)

      ClusterStoreActions.getDiscoveringClusters()
      ClusterStoreActions.getDiscoveredClusters()

    }

    def editCluster(item: ClusterForm): Unit = {
      log.debug("item " + item)
      ClusterStoreActions.subscribeToCluster(item.name, item.seeds.map(HostPortUtil.apply))
    }

    def selectCluster(name: String) = {
      ClusterStoreActions.getCluster(name)
    }
  }

  // create the React component for Clusters mgmt
  val component = ReactComponentB[Props]("Clusters")
    .initialState(State()) // initial state from TodoStore
    .backend(new Backend(_))
    .render((P, S, B) => {

      log.debug("!!!!!!!!!!!!!!!!!!!   render ClusterMap seelcted = " + P.store.getSelectedCluster)

      div(cls := "row")(
        div(cls := "col-md-4")(
          ClusterFormComponent(P.store, B.editCluster),
          div(
            h3("Discovering Clusters"),
            div(
              P.store.getDiscoveringClusters().values.map(e =>
                DiscoveringClusterComponent(e)
              ))
          ),
          div(
            h3("Discovered Clusters"),
            div(
              P.store.getDiscoveredClusters().values.map(e =>
                DiscoveredClusterComponent(e, P.store.getSelectedCluster().contains(e.system), B.selectCluster)
              ))
          ),
          div(
            h3("Discovered Cluster Events"),
            ActivityLogComponent(ActivityLogService)
          )
        ),
        div(cls := "col-md-8")(
          h3("Cluster map"),
          P.store.getSelectedCluster().fold[TagMod](EmptyTag)(item =>
            ClusterNodeGraphComponent(P.store, item.system)
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
