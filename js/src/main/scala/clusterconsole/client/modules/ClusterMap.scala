package clusterconsole.client.modules

import clusterconsole.client.ClusterConsoleApp.Loc
import clusterconsole.client.components.{ClusterNodeGraphComponent, ClusterFormComponent}
import clusterconsole.client.services.{ClusterStoreActions, ClusterStore, RefreshClusterMembers, MainDispatcher}
import clusterconsole.http.{ClusterForm, DiscoveredCluster, ClusterMember}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.all._
import clusterconsole.client.services.Logger._
import rx._
import rx.ops._


object ClusterMap {

  case class Props(clusters: Rx[Map[String,DiscoveredCluster]], router: RouterCtl[Loc])

  case class State(selectedItem: Option[DiscoveredCluster] = None)

  abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {
    protected def observe[T](rx: Rx[T]): Unit = {
      val obs = rx.foreach(_ => scope.forceUpdate())
      // stop observing when unmounted
      onUnmount(obs.kill())
    }
  }

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      // hook up to TodoStore changes
      observe(t.props.clusters)
      // dispatch a message to refresh the todos, which will cause TodoStore to fetch todos from the server
//      MainDispatcher.dispatch(RefreshClusterMembers)
    }

    def editCluster(item: ClusterForm):Unit = {
      log.debug("item " + item)
      ClusterStoreActions.subscribeToCluster(ClusterStore, item.name, item.seeds)
    }
  }

  // create the React component for Clusters mgmt
  val component = ReactComponentB[Props]("Clusters")
    .initialState(State()) // initial state from TodoStore
    .backend(new Backend(_))
    .render((P, S, B) => {
    div(cls := "row")(
      div(cls := "col-md-4")(
        ClusterFormComponent(ClusterForm.initial,B.editCluster),
        div{
          P.clusters().map(e =>
            div(key:=e._1)(
              span(e._1),span(e._2.toString)
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
    component(Props(store.clusterMembers, router))
  }


}
