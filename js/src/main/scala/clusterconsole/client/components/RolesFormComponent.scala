package clusterconsole.client.components

import clusterconsole.client.modules.RxObserver
import clusterconsole.client.services.Logger
import clusterconsole.client.style.Bootstrap.{ Button, Modal }
import clusterconsole.client.style.Icon
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{ ReactComponentB, BackendScope }
import Logger._

object RolesFormComponent {

  case class Props(closeForm: () => Unit)

  case class State()

  class Backend(t: BackendScope[Props, State]) {

    def hide() = {
      log.debug("RolesFormComponent hide()")
      t.props.closeForm
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

      Modal(Modal.Props(
        header = be => span(button(tpe := "button", cls := "pull-right", onClick --> be.hide(), Icon.close), h4("Describe Dependencies")),
        footer = be => span(Button(Button.Props(() => { B.submitForm; be.hide() }), "OK")),
        closed = () => P.closeForm()),
        div(cls := "row")("Sdfdsf")
      )
    ).build

  def apply(closeForm: () => Unit) = component(Props(closeForm))

}
