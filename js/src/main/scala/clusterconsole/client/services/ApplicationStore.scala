package clusterconsole.client.services

import clusterconsole.client.ukko.Actor
import org.scalajs.dom.ext.Ajax
import rx._
import Logger._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

case object RefreshVersion

trait ApplicationStore extends Actor {

  override val name = "ApplicationStore"

  private val version = Var("")

  private def updateVersion(v: String) = {

    log.debug("updateVersion v " + v)

    version() = v
  }

  override def receive = {
    case RefreshVersion =>
      Ajax.get("/version").foreach { xhr =>
        updateVersion(upickle.read[String](xhr.responseText))
      }
    case _ => {}
  }

  // return as Rx to prevent mutation in dependencies
  def getVersion: Rx[String] = version

}

// create a singleton instance of TodoStore
object ApplicationStore extends ApplicationStore {
  // register this actor with the dispatcher
  log.debug("Registering ApplicationStore")
  MainDispatcher.register(this)
}

