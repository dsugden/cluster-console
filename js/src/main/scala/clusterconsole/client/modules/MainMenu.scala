package clusterconsole.client.modules

import clusterconsole.client.ClusterConsoleApp.{ClusterMapLoc, DashboardLoc, Loc}
import clusterconsole.client.style.{Icon, GlobalStyles}
import Icon.Icon
import clusterconsole.client.style.{Icon, GlobalStyles}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.all._
import rx._
import rx.ops._

object MainMenu {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(ctl: RouterCtl[Loc], currentLoc:Loc)

  case class MenuItem(label: (Props) => ReactNode, icon: Icon, location: Loc)

  class Backend(t: BackendScope[Props, _]) extends OnUnmount {
    def mounted(): Unit = {
//      // hook up to Todo changes
//      val obsItems = t.props.todos.foreach { _ => t.forceUpdate() }
//      onUnmount {
//        // stop observing when unmounted (= never in this SPA)
//        obsItems.kill()
//      }
//      MainDispatcher.dispatch(RefreshTodos)
    }
  }


  private val menuItems = Seq(
    MenuItem(_ => "Dashboard", Icon.dashboard, DashboardLoc),
    MenuItem(_ => "Dashboard", Icon.check, ClusterMapLoc)
  )

  private val MainMenu = ReactComponentB[Props]("MainMenu")
    .stateless
    .backend(new Backend(_))
    .render((P, _, B) => {
    ul(
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
