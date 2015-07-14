package clusterconsole.client

import clusterconsole.client.modules.{MainMenu, ClusterMap, Dashboard}
import clusterconsole.client.services.{AjaxClient, ClusterStore}
import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{HostPort, Api, ClusterSubscribe}
import japgolly.scalajs.react.React
import japgolly.scalajs.react.extra.router2._
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scalacss.Defaults._
import scalacss.ScalaCssReact._
import clusterconsole.client.services.Logger._
import scala.concurrent.ExecutionContext.Implicits.global
import autowire._


@JSExport("ClusterConsoleApp")
object ClusterConsoleApp extends js.JSApp{


  // Define the locations (pages) used in this application
  sealed trait Loc

  case object DashboardLoc extends Loc

  case object ClusterMapLoc extends Loc

  // configure the router
  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    (staticRoute(root, DashboardLoc) ~> renderR(ctl => Dashboard.component(ctl))
      | staticRoute("#clustermap", ClusterMapLoc) ~> renderR(ctl => ClusterMap(ClusterStore)(ctl))
      ).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))
  }.renderWith(layout)

  // base layout for all pages
  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      nav(className := "navbar navbar-inverse navbar-fixed-top")(
        div(className := "container")(
          div(className := "navbar-header")(span(className := "navbar-brand")("Cluster Console")),
          div(className := "collapse navbar-collapse")(
            MainMenu(MainMenu.Props(c, r.page))
          )
        )
      ),
      // currently active module is shown in this container
      div(className := "container")(r.render())
    )
  }


  @JSExport
  def main(): Unit = {
    log.warn("Application starting")

    // create stylesheet
    GlobalStyles.addToDocument()
    // create the router
    val router = Router(BaseUrl(dom.window.location.href.takeWhile(_ != '#')), routerConfig)
    // tell React to render the router in the document body
    React.render(router(), dom.document.body)


    AjaxClient[Api].discover("testclusterName", List(HostPort("host",9000))).call().foreach( s =>
      log.debug("$$$$$$$$$$$$$  result " + s)
    )





  }




}
