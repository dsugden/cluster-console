package clusterconsole.core

import com.typesafe.scalalogging.{ Logger, LazyLogging }

import scala.language.implicitConversions

/**
 * @author patrick.premont@boldradius.com
 */
trait LogF extends LazyLogging {
  implicit val loggerF = logger
  def withValue[V](v: V)(f: V => Unit): V = { f(v); v }
  implicit def toLogging[V](v: V): Logging[V] = Logging(v)

  def time[A](f: => A): (A, Long) = {
    val s = System.currentTimeMillis; val v: A = f; (v, System.currentTimeMillis - s)
  }
  def logTime[A](show: Long => String)(f: => A): A = time(f).logTest { case (_, t) => show(t) }._1

}

case class Logging[V](v: V) extends LazyLogging {
  def logInfo(f: V => String)(implicit loggerF: Logger): V = { loggerF.info(f(v)); v }
  def logDebug(f: V => String)(implicit loggerF: Logger): V = { loggerF.debug(f(v)); v }
  def logError(f: V => String)(implicit loggerF: Logger): V = { loggerF.error(f(v)); v }

  def logTest(f: V => String)(implicit loggerF: Logger): V = { loggerF.debug(f(v)); v }
}
