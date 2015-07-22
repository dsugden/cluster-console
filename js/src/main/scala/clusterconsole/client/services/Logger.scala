package clusterconsole.client.services

import scala.scalajs.js.Dynamic.global

class Logger {
  def info(s: String): Unit = {
    global.console.log(s"[INFO] $s")
  }
  def debug(s: String): Unit = {
    global.console.log(s"[DEBUG] $s")
  }
  def warn(s: String): Unit = {
    global.console.log(s"[WARN] $s")
  }
  def error(s: String): Unit = {
    global.console.log(s"[ERROR] $s")
  }

}

object Logger {

  lazy val log: Logger = new Logger()

}
