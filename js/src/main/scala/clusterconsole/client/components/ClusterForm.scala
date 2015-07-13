package clusterconsole.client.components

import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.DiscoveredCluster
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{ReactComponentB, BackendScope}


object ClusterForm {

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class EditClusterProps(cluster: Option[DiscoveredCluster], editHandler: Option[DiscoveredCluster] => Unit)

  case class State(cluster:Option[DiscoveredCluster])

  class Backend(t: BackendScope[EditClusterProps, State]) {
  }


  val component = ReactComponentB[EditClusterProps]("ClusterForm")
    .initialState(State(None)) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {
    div(cls := "row")(
      div(cls := "col-md-12")(
        h3("Cluster form"),
        form(
          div(cls := "form-group")(
            label("Cluster Name"),
            input(tpe := "text", cls := "form-control")
          ),
          div(cls := "form-group")(
            label("Cluster Seed"),
            input(tpe := "text", cls := "form-control")
          )
        )
      )
    )
  }).build

  def apply(cluster: Option[DiscoveredCluster], editHandler: Option[DiscoveredCluster] => Unit) = component(EditClusterProps(cluster,editHandler))



}
