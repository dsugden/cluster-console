package clusterconsole.client.modules

import clusterconsole.client.ClusterConsoleApp.Loc
import clusterconsole.client.components.{ ClusterFormComponent, ClusterNodeGraphComponent }
import clusterconsole.client.services.Logger._
import clusterconsole.client.services.{ ClusterStore, ClusterStoreActions }
import clusterconsole.http.{ HostPortUtil, HostPort, ClusterForm, DiscoveredCluster }
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.all._
import rx._

object ClusterMap {

  case class Props(store: ClusterStore, router: RouterCtl[Loc])

  case class State(selectedItem: Option[DiscoveredCluster] = None)

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      observe(t.props.store.clusterMembers)
    }

    def editCluster(item: ClusterForm): Unit = {
      log.debug("item " + item)
      ClusterStoreActions.subscribeToCluster(ClusterStore, item.name, item.seeds.map(HostPortUtil.apply))
    }
  }

  // create the React component for Clusters mgmt
  val component = ReactComponentB[Props]("Clusters")
    .initialState(State()) // initial state from TodoStore
    .backend(new Backend(_))
    .render((P, S, B) => {
      div(cls := "row")(
        div(cls := "col-md-4")(
          ClusterFormComponent(P.store, B.editCluster),
          div {
            P.store.clusterMembers().map(e =>
              div(key := e._1)(
                span(e._1), span(e._2.toString)
              )
            )

          }
        ),
        div(cls := "col-md-8")(
          h3("Cluster map"),
          ClusterNodeGraphComponent()
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
