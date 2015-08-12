package clusterconsole.client.services

import clusterconsole.client.ukko.Actor
import clusterconsole.http.{ ClusterEvent, ClusterProtocol }
import clusterconsole.client.services.Logger._
import rx._

object ActivityLogService extends ActivityLogService {

  def init = {
    log.debug("init ActivityLogService")
    MainDispatcher.register(this)
  }

}

trait ActivityLogService extends Actor {

  private val logItems = Var(Seq.empty[ClusterEvent])

  def activities: Rx[Seq[ClusterEvent]] = logItems

  def name = "ActivityLogService"

  def receive: Receive = {
    case ac: ClusterEvent =>
      log.debug(s"ActivityLogService: $ac")
      logItems() = ac +: logItems()

    case other =>
  }
}
