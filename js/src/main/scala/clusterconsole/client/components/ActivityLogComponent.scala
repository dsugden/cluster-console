package clusterconsole.client.components

import clusterconsole.client.modules.RxObserver
import clusterconsole.client.services.ActivityLogService
import clusterconsole.http.{ ClusterEvent, ClusterEventUtil, ClusterProtocol }
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{ BackendScope, ReactComponentB }
import rx._

object ActivityLogComponent {

  case class Props(activities: Rx[Seq[ClusterEvent]])
  case class State(logItems: Seq[ClusterEvent] = Seq.empty)

  def apply(service: ActivityLogService) = {
    component(Props(service.activities))
  }

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      observe(t.props.activities)
      // MainDispatcher.dispatch(RefreshClusterMembers)
    }
  }

  val component = ReactComponentB[Props]("ActivityLog")
    .initialState(State())
    .backend(new Backend(_))
    .render((P, S, B) => {

      div(
        h3("Discovered Cluster Events"),
        div(cls := "col-md-12")(
          P.activities().map(ac => div(ClusterEventUtil.label(ac)))
        )
      )

    })
    .componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

}
