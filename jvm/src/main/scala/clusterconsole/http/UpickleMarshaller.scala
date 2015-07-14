package clusterconsole.http

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import upickle.{ Writer, Reader }

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration._

import Json._

/**
 * @author Dave Sugden
 *         Dmitri Carpov
 */

object UpickleMarshaller extends UpickleMarshaller

trait UpickleMarshaller {
  implicit def upickleFromRequestUnmarshaller[T: Reader](implicit fm: Materializer): FromRequestUnmarshaller[T] =
    new Unmarshaller[HttpRequest, T] {
      def apply(req: HttpRequest)(implicit ec: ExecutionContext): Future[T] = {
        req.entity.withContentType(ContentTypes.`application/json`).toStrict(1.second)
          .map(_.data.toArray).map(x => upickle.read[T](new String(x)))
      }
    }

  implicit def upickleFromResponseUnmarshaller[T: Reader](implicit fm: Materializer): FromResponseUnmarshaller[T] =
    new Unmarshaller[HttpResponse, T] {
      def apply(res: HttpResponse)(implicit ec: ExecutionContext): Future[T] = {
        res.entity.withContentType(ContentTypes.`application/json`).toStrict(1.second).map(_.data.toArray).map(x => upickle.read[T](new String(x)))
      }
    }

  implicit def upickleToResponseMarshaller[T: Writer](implicit fm: Materializer): ToResponseMarshaller[T] =
    Marshaller.withFixedCharset[T, MessageEntity](MediaTypes.`application/json`, HttpCharset.custom("*"))(tp =>
      upickle.write[T](tp))

  implicit def upickleToEntityMarshaller[T: Writer]: ToEntityMarshaller[T] =
    Marshaller.withFixedCharset[T, MessageEntity](MediaTypes.`application/json`, HttpCharset.custom("*"))(tp =>
      HttpEntity(upickle.write[T](tp)))
}
