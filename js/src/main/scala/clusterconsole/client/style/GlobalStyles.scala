package clusterconsole.client.style

import scala.language.postfixOps
import scalacss.Defaults._

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  style(unsafeRoot("body")(
    paddingTop(50.px))
  )

  val bootstrapStyles = new BootstrapStyles
}
