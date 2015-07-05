package clusterconsole.client.style

import scala.language.postfixOps
import scalacss.Defaults._

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

//  style(unsafeRoot("body")(
//    paddingTop(50.px))
//  )


  val topNav = style(
    height(100 px)
  )


  val valign = style(addClassName("valign-wrapper"))

//  "nav.top-nav" - (
//      height(200 px )
//
//    )

  val bootstrapStyles = new BootstrapStyles
}
