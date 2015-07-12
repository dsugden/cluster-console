package clusterconsole.http

import upickle.Js

object Json {

  implicit val clusterProtocolWriter = upickle.Writer[ClusterProtocol] {

    case r: TestResponse =>
      Js.Arr(
        Js.Str("1"),
        Js.Str(upickle.write[TestResponse](r))
      )

    case r: ClusterMemberUp =>
      Js.Arr(
        Js.Str("2"),
        Js.Str(upickle.write[ClusterMemberUp](r))
      )


    case _ =>  Js.Str("BAD")
  }


  implicit  val clusterProtocolReader = upickle.Reader[ClusterProtocol] {

    case Js.Arr(Js.Str("1"), Js.Str(v)) =>
      upickle.read[TestResponse](v)


    case Js.Arr(Js.Str("2"), Js.Str(v)) =>
      upickle.read[ClusterMemberUp](v)


    case _ => TestResponse("BAD")



  }


}
