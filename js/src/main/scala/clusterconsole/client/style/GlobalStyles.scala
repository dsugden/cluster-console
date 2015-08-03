package clusterconsole.client.style

import scala.language.postfixOps
import scalacss.Defaults._

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  val textColor = "#EAD0D0"

  val navUnselectedTextColor = "#8ED5EA"

  val mainHeaderColor = "#2C3138"

  val leftNavBackgrounColor = "#39484E"

  val mapBackground = "#353131"

  val nodeUpColor = "#2FA02B"
  val nodeUnreachableColor = "#D46415"
  val nodeRemovedColor = "#B91414"

  val common = mixin(
    backgroundColor(mapBackground)
  )

  val button = style(
    padding(0.5 ex, 2 ex),
    backgroundColor("#eee"),
    border(1 px, solid, black)
  )

  val leftNav = style("leftNav")(
    border(1 px, solid, white),
    backgroundColor("#39484E")
  )

  val mainHeaders = style("mainHeaders")(
    backgroundColor(mainHeaderColor),
    borderColor(white),
    borderBottom(1 px, solid)
  )

  val regText = style("regText")(
    color(textColor)
  )

  style(

    unsafeRoot("body")(
      common,
      paddingTop(50.px),

      unsafeChild("h3")(
        color(textColor)
      ),
      unsafeChild("h2")(
        color(textColor)
      ),
      unsafeChild("h4")(
        color(textColor)
      )

    )

  )

  val bootstrapStyles = new BootstrapStyles
}
