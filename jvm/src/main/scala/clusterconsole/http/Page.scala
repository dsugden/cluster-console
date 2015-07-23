package clusterconsole.http

import scalatags.Text.all.{ html => htmlFrag, _ }
import scalatags.Text.tags2
import scalatags.stylesheet.{ StyleSheet, Sheet }

object Page {

  private[Page] def headTag(title: String) =
    head(
      tags2.title(id := "title")(title),
      meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
      link(href := "stylesheets/main.min.css", rel := "stylesheet", tpe := "text/css"),
      tags2.style(raw("""
          |.link {
          |  stroke: #000;
          |  stroke-width: 1.5px;
          |}
          |
          |.node {
          |  cursor: move;
          |  fill: #ccc;
          |  stroke: #000;
          |  stroke-width: 1.5px;
          |}
          |
          |.node.fixed {
          |  fill: #f00;
          |}
        """.stripMargin))
    )

  def main(title: String, content: Frag*) =
    htmlFrag(
      headTag(title),
      body(onload := "ClusterConsoleApp().main()")(
        script(src := "/js/cluster-console-jsdeps.js"),
        script(src := "/js/cluster-console-fastopt.js")
      )
    )
}

