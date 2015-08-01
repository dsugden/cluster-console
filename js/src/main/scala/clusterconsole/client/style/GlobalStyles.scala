package clusterconsole.client.style

import scala.language.postfixOps
import scalacss.Defaults._

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  val textColor = "#EAD0D0"

  val common = mixin(
    backgroundColor("#353131")
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
    backgroundColor("#2C3138"),
    borderBottom(1 px, solid, white)
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
      )
    )

  )

  val bootstrapStyles = new BootstrapStyles
}
