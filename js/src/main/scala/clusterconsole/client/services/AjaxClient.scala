package clusterconsole.client.services

import clusterconsole.http.Json
import org.scalajs.dom

import scala.concurrent.{ Future, Promise }
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import upickle.default._

object AjaxClient extends autowire.Client[String, Reader, Writer] {

  import org.scalajs.dom.ext.AjaxException

  import Json._

  def post(url: String,
    data: String,
    timeout: Int = 0) = {

    val req = new dom.XMLHttpRequest()
    val promise = Promise[dom.XMLHttpRequest]()

    req.onreadystatechange = { (e: dom.Event) =>
      if (req.readyState == 4) {
        if ((req.status >= 200 && req.status < 300) || req.status == 304)
          promise.success(req)
        else
          promise.failure(AjaxException(req))
      }
    }
    req.open("POST", url)
    req.timeout = timeout
    req.send(data)
    promise.future
  }

  override def doCall(req: Request): Future[String] = {
    post(url = "/api/" + req.path.mkString("/"),
      data = write[Map[String, String]](req.args)
    ).map(_.responseText)
  }

  def write[Result: Writer](r: Result) = upickle.default.write(r)
  def read[Result: Reader](p: String) = upickle.default.read[Result](p)
}
