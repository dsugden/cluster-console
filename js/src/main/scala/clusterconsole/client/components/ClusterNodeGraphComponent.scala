package clusterconsole.client.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all.svg._
import japgolly.scalajs.react.vdom.prefix_<^._
import scala.scalajs.js
import clusterconsole.client.d3._
import js.JSConverters._
import clusterconsole.client.services.Logger._

//object ActionAssignment {
//
//  import clusterconsole.client.style.CustomTags._
//
//
//  val component = ReactComponentB[Unit]("ActionAssignment")
//    .render(P => {
//    svgtag(
//      path(^.key := "acg", d := "M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z")
//    )
//  }).buildU
//
//  def apply() = component()
//}

object ClusterNodeGraphComponent {

  case class Props()

  case class State()

  class Backend(t: BackendScope[Props, State]) {

  }

  def component = ReactComponentB[Props]("ClusterNodeGraph")
    .initialStateP(P => {
      State()
    }).backend(new Backend(_))
    .render((P, S, B) => {
      BarChart(600, 1000)
    }).build

  def apply() = component(Props())

}
