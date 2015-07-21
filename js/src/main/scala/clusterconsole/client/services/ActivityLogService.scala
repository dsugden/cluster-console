package clusterconsole.client.services

import clusterconsole.client.ukko.Actor
import clusterconsole.http.ClusterProtocol
import clusterconsole.client.services.Logger._
import rx._

object ActivityLogService extends ActivityLogService {

  def init = {
    log.debug("init ActivityLogService")
    MainDispatcher.register(this)
  }

}

trait ActivityLogService extends Actor {

  private val logItems = Var(Seq.empty[ClusterProtocol])

  def activities:Rx[Seq[ClusterProtocol]] = logItems

  def name = "ActivityLogService"

  def receive: Receive = {
    case ac: ClusterProtocol =>
      log.debug(s"ActivityLogService: $ac")
      logItems() = logItems() :+ ac


    case other =>
  }
}
