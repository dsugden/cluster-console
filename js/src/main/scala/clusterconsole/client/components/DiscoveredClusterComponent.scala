package clusterconsole.client.components

import clusterconsole.client.components.ClusterFormComponent.{ EditClusterProps, State }
import clusterconsole.client.components.Graph.State
import clusterconsole.client.components.Graph.State
import clusterconsole.client.d3._
import clusterconsole.client.modules.{ Roles, Mode, RxObserver }
import clusterconsole.client.services.{ ClusterStoreActions, ClusterStore }
import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{ DiscoveryBegun, ClusterForm, HostPort, DiscoveredCluster }
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import clusterconsole.client.services.Logger._
import rx._

import scala.scalajs.js

object DiscoveredClusterComponent {

  @inline private def globalStyles = GlobalStyles

  case class Props(discovered: Rx[Map[String, DiscoveredCluster]], selected: Rx[Option[DiscoveredCluster]], mode: Mode)

  case class State(rolesOpen: Option[String])

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {

    def mounted(): Unit = {
      observe(t.props.discovered)
      observe(t.props.selected)

    }

    def selectCluster(e: ReactMouseEvent) = {
      ClusterStoreActions.selectCluster(e.currentTarget.firstChild.nodeValue)
      e.preventDefault()
    }

    def roles(system: String) = {
      t.modState(_.copy(rolesOpen = Some(system)))
    }

  }

  val component = ReactComponentB[Props]("DiscoveredClusterComponent")
    .initialStateP(P => {
      State(None)
    }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {

      div(paddingTop := "30px")(
        if (P.discovered().isEmpty) {
          span("")
        } else {
          div(cls := "row", height := "200px")(
            if (S.rolesOpen.isDefined) {
              RolesFormComponent()
            } else {
              span("")
            },
            div(cls := "col-md-12")(
              div(cls := "row", borderBottom := "1px solid white")(
                div(cls := "col-md-12")(
                  span(fontSize := "20px", color := globalStyles.textColor)("Discovered"))),
              div(cls := "row")(
                P.discovered().values.map(e =>

                  if (isSelected(P, e.system) && P.mode == Roles) {
                    div(cls := "col-md-12", paddingTop := "5px", paddingBottom := "5px", backgroundColor := selectedBackground(P, e.system))(
                      a(href := "", key := e.system)(
                        span(onClick ==> B.selectCluster,
                          color := selectedColor(P, e.system))(e.system)
                      ), span(cls := "pull-right")(button(onClick --> B.roles(e.system))("Roles"))
                    )

                  } else {
                    div(cls := "col-md-12", paddingTop := "5px", paddingBottom := "5px", backgroundColor := selectedBackground(P, e.system))(
                      a(href := "", key := e.system)(
                        span(onClick ==> B.selectCluster,
                          color := selectedColor(P, e.system))(e.system)
                      )
                    )

                  }
                )
              )
            )
          )
        }
      )

    }
    ).componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  def selectedColor(props: Props, system: String) =
    if (isSelected(props, system)) {
      globalStyles.textColor
    } else {
      globalStyles.navUnselectedTextColor
    }

  def selectedBackground(props: Props, system: String) =
    if (isSelected(props, system)) {
      "#6A777B"
    } else {
      ""
    }

  def isSelected(props: Props, system: String): Boolean =
    props.selected().exists(_.system == system)

  def apply(discovered: Rx[Map[String, DiscoveredCluster]],
    selected: Rx[Option[DiscoveredCluster]],
    mode: Mode) = component(Props(discovered, selected, mode))

}
