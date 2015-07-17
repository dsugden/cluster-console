package clusterconsole.client.modules

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.extra.OnUnmount
import rx._
import rx.ops._  //implicit foreach on rx

abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {
    protected def observe[T](rx: Rx[T]): Unit = {
      val obs = rx.foreach(_ => scope.forceUpdate())
      // stop observing when unmounted
      onUnmount(obs.kill())
    }
  }
