package clusterconsole.client.modules

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.extra.OnUnmount
import rx._
import rx.ops._ //implicit foreach on rx
import clusterconsole.client.services.Logger._

import clusterconsole.client.services.Logger._

abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {
  protected def observe[T](rx: Rx[T]): Unit = {
    val obs = rx.foreach(_ => scope.forceUpdate(), true)
    // stop observing when unmounted
    onUnmount(obs.kill())
  }

  protected def react[T](rx: Rx[T], update: T => Unit): Unit = {
    val obs = rx.foreach(v => {

      log.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  react    " + v.toString)

      update(v)

    }, true)
    // stop observing when unmounted
    onUnmount(obs.kill())
  }

}
