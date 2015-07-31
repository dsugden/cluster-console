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
      //      observe(t.props.store.getDiscoveredClusters)
      //      observe(t.props.store.getDiscoveringClusters)
      //      observe(t.props.store.getSelectedCluster)
      //      observe(t.state.selectedItem)

      ClusterStoreActions.getDiscoveredClusters()

    }

    def editCluster(item: ClusterForm): Unit = {
      log.debug("item " + item)
      ClusterStoreActions.subscribeToCluster(item.name, item.seeds.map(HostPortUtil.apply))
    }

    def selectCluster(name: String) = {
      ClusterStoreActions.selectCluster(name)
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
          DiscoveringClusterComponent(P.store.getDiscoveringClusters),
          DiscoveredClusterComponent(P.store.getDiscoveredClusters, P.store.getSelectedCluster),
          ActivityLogComponent(ActivityLogService)
        ),
        div(cls := "col-md-8")(
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
