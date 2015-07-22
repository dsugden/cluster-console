package clusterconsole.client.modules

import clusterconsole.client.ClusterConsoleApp.Loc
import clusterconsole.client.services.ActivityLogService
import clusterconsole.http.ClusterProtocol
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{ BackendScope, ReactComponentB }
import rx._

object ActivityLogComponent {

  case class Props(activities: Rx[Seq[ClusterProtocol]], router: RouterCtl[Loc])
  case class State(logItems: Seq[ClusterProtocol] = Seq.empty)

  def apply(service: ActivityLogService) = (router: RouterCtl[Loc]) => {
    component(Props(service.activities, router))
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
      div(cls := "row")(
        div(cls := "col-md-12")(
          P.activities().map(ac => div(ac.toString))
        //ClusterFormComponent(ClusterForm.initial,B.editCluster),
        /*div{
          P.clusters().map(e =>
            div(key:=e._1)(
              span(e._1),span(e._2.toString)
            )
          )

        }*/
        )
      )
    })
    .componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

}
