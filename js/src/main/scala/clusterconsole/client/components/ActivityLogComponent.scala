package clusterconsole.client.components

import clusterconsole.client.modules.RxObserver
import clusterconsole.client.services.ActivityLogService
import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{ ClusterEvent, ClusterEventUtil, ClusterProtocol }
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{ ReactMouseEvent, BackendScope, ReactComponentB }
import rx._

object ActivityLogComponent {

  @inline private def globalStyles = GlobalStyles

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

    def select(e: ReactMouseEvent) = {
      e.preventDefault()
    }

  }

  val component = ReactComponentB[Props]("ActivityLog")
    .initialState(State())
    .backend(new Backend(_))
    .render((P, S, B) => {

      div(paddingTop := "30px")(
        div(cls := "row", height := "200px")(
          div(cls := "col-md-12")(
            div(cls := "row", borderBottom := "1px solid white")(
              div(cls := "col-md-12")(
                span(fontSize := "20px", color := globalStyles.textColor)("Events"))),
            div(cls := "row")(
              div(cls := "col-md-12", paddingTop := "10px")(
                P.activities().map(e =>
                  a(href := "", key := e.system)(
                    span(onClick ==> B.select)(ClusterEventUtil.label(e))
                  )
                )))
          )
        )
      )
    })
    .componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

}

