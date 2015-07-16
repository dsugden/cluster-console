package clusterconsole.client.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all._

object ClusterNodeGraphComponent {

  case class Props()

  case class State()

  class Backend(t: BackendScope[Props, State]) {

  }


  def component = ReactComponentB[Props]("ClusterNodeGraph")
    .initialStateP(P => {
    State()

  }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {
    div(cls := "row")(
    "Soon to come d3 node graph"
    )
  }).build

  def apply() = component(Props())
  

}
