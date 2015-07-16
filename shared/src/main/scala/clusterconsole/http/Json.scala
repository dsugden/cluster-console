package clusterconsole.http

import upickle.Js

object Json {

  implicit val clusterProtocolWriter = upickle.Writer[ClusterProtocol] {

    case r: TestResponse =>
      Js.Arr(
        Js.Num(1),
        Js.Str(upickle.write[TestResponse](r))
      )

    case r: ClusterMemberUp =>
      Js.Arr(
        Js.Num(2),
        Js.Str(upickle.write[ClusterMemberUp](r))
      )

    case r: ClusterUnjoin =>
      Js.Arr(
        Js.Num(3),
        Js.Str(upickle.write[ClusterUnjoin](r))
      )

    case other =>  Js.Str("Json error " + other)
  }


  implicit  val clusterProtocolReader = upickle.Reader[ClusterProtocol] {

    case Js.Arr(Js.Num(1), Js.Str(v)) =>
      upickle.read[TestResponse](v)


    case Js.Arr(Js.Num(2), Js.Str(v)) =>
      upickle.read[ClusterMemberUp](v)

    case Js.Arr(Js.Num(3), Js.Str(v)) =>
      upickle.read[ClusterUnjoin](v)

    case _ => TestResponse("BAD")



  }


}
