package clusterconsole.client.components

import clusterconsole.client.modules.RxObserver
import clusterconsole.client.services.ClusterStoreActions
import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{ DiscoveryBegun, DiscoveredCluster }
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.all._
import rx._
import scalacss.ScalaCssReact._

object DiscoveringClusterComponent {

  @inline private def globalStyles = GlobalStyles

  case class Props(discovering: Rx[Map[String, DiscoveryBegun]])

  case class State()

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      observe(t.props.discovering)
    }

    def select(e: ReactMouseEvent) = {
      e.preventDefault()
    }

  }

  val component = ReactComponentB[Props]("DiscoveringClusterComponent")
    .initialStateP(P => {
      State()
    }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {

      div(paddingTop := "30px")(
        if (P.discovering().isEmpty) {
          span("")
        } else {
          div(cls := "row", height := "200px")(
            div(cls := "col-md-12")(
              div(cls := "row", borderBottom := "1px solid white")(
                div(cls := "col-md-12")(
                  span(fontSize := "20px", color := globalStyles.textColor)("Discovery in Progress"))),
              div(cls := "row")(
                P.discovering().values.map(e =>
                  div(cls := "col-md-12", paddingTop := "5px", paddingBottom := "5px")(
                    a(href := "", key := e.system, onClick ==> B.select)(
                      span(color := globalStyles.navUnselectedTextColor, fontSize := "15px")(
                        span(e.system), span(e.seedNodes.map(hp => hp.host + ":" + hp.port))
                      )
                    )
                  )
                ))
            )
          )
        }
      )
    }
    ).componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def apply(discovering: Rx[Map[String, DiscoveryBegun]]) = component(Props(discovering))

}
