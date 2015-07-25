package clusterconsole.client.modules

import clusterconsole.client.ClusterConsoleApp.{ ClusterMapLoc, DashboardLoc, Loc }
import clusterconsole.client.style.Icon.Icon
import clusterconsole.client.style.{ GlobalStyles, Icon }
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.all._

import scalacss.ScalaCssReact._

object MainMenu {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(ctl: RouterCtl[Loc], currentLoc: Loc)

  case class MenuItem(label: (Props) => ReactNode, icon: Icon, location: Loc)

  class Backend(t: BackendScope[Props, _]) extends OnUnmount {
    def mounted(): Unit = {
    }
  }

  private val menuItems = Seq(
    MenuItem(_ => "Dashboard", Icon.dashboard, DashboardLoc),
    MenuItem(_ => "ClusterMap", Icon.circle, ClusterMapLoc)
  )

  private val MainMenu = ReactComponentB[Props]("MainMenu")
    .stateless
    .backend(new Backend(_))
    .render((P, _, B) => {
      ul(bss.navbar)(
        // build a list of menu items
        for (item <- menuItems) yield {
          li((P.currentLoc == item.location) ?= (className := "active"),
            P.ctl.link(item.location)(item.icon, " ", item.label(P))
          )
        }
      )
    })
    .componentDidMount(_.backend.mounted())
    .build

  def apply(props: Props) = MainMenu(props)
}
