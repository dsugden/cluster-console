package clusterconsole.client.components

import clusterconsole.client.modules.RxObserver
import clusterconsole.client.style.Bootstrap.{ Button, Modal }
import clusterconsole.client.style.Icon
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{ ReactComponentB, BackendScope }

object RolesFormComponent {

  case class Props()

  case class State()

  class Backend(t: BackendScope[Props, State]) {

    def hide() = {

    }

    def submitForm() = {

    }

  }

  val component = ReactComponentB[Props]("DiscoveringClusterComponent")
    .initialStateP(P =>
      State()
    )
    .backend(new Backend(_))
    .render((P, S, B) =>

      Modal(Modal.Props(be => span(button(tpe := "button", cls := "pull-right", onClick --> be.hide(), Icon.close), h4("Describe Dependencies")),
        be => span(Button(Button.Props(() => { B.submitForm; be.hide() }), "OK")),
        () => B.hide),
        div(cls := "row")()

      )
    ).build

  def apply() = component(Props())

}
