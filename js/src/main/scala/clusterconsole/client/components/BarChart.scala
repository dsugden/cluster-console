package clusterconsole.client.components

import japgolly.scalajs.react.{ ReactComponentB, ReactNode }

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.all.svg._
import japgolly.scalajs.react.vdom.prefix_<^._
import scala.scalajs.js
import clusterconsole.client.d3._
import js.JSConverters._

import scala.scalajs.js

object Chart {

  import clusterconsole.client.style.CustomTags._

  case class Props(width: Double, height: Double, children: ReactNode*)

  val component = ReactComponentB[Props]("Chart")
    .render(P => {
      svgtag(width := P.width, height := P.height)(
        P.children
      )
    }).build

  def apply(width: Double, height: Double, children: ReactNode*) = component(Props(width, height, children))

}

object Bar {

  case class Props(width: Double = 0, height: Double = 0, color: String, offset: Double = 0, availableHeight: Double)

  val component = ReactComponentB[Props]("Bar")
    .render(P => {
      rect(fill := P.color, width := P.width, height := P.height, x := P.offset, y := P.availableHeight - P.height)
    }).build

  def apply(width: Double, height: Double, color: String, offset: Double, availableHeight: Double) =
    component(Props(width, height, color, offset, availableHeight))

}

object DataSeries {

  case class Props(data: List[Double], height: Double, width: Double, color: String)

  val component = ReactComponentB[Props]("DataSeries")
    .render(P => {

      val yScale = d3.scale
        .linear()
        .domain(List[js.Any](0, d3.max[Double](P.data.toJSArray)).toJSArray)
        .range(List[js.Any](0, P.height).toJSArray)

      val xScale = d3.scale.ordinal()
        .domain(d3.range(P.data.length).map(x => x: js.Any))
        .rangeRoundBands(List[js.Any](0, P.width).toJsArray, 0.05, 0.05)

      val bars = P.data.zipWithIndex.map {
        case (point, i) =>

          Bar(
            width = xScale.rangeBand(),
            height = yScale(point),
            offset = xScale(i).asInstanceOf[Double],
            availableHeight = P.height,
            color = P.color
          )
      }
      g(bars)
    }).build

  def apply(data: List[Double], height: Double, width: Double, color: String) =
    component(Props(data, height, width, color))

}

object BarChart {

  case class Props(height: Double, width: Double)

  val component = ReactComponentB[Props]("BarChart")
    .render(P =>

      Chart(
        width = P.width,
        height = P.height,
        DataSeries(
          data = List(30, 10, 5, 8, 15, 10),
          width = P.width,
          height = P.height,
          color = "blue"
        )
      )

    ).build

  def apply(width: Double, height: Double) =
    component(Props(width, height))

}
