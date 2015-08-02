package clusterconsole.http

import upickle.default._
import upickle._

object Json {

  implicit val clusterProtocolWriter = Writer[ClusterProtocol] {

    case r: CurrentClusterStateInitial =>
      Js.Arr(
        Js.Num(1),
        Js.Str(write[CurrentClusterStateInitial](r))
      )


    case r: ClusterMemberUp =>
      Js.Arr(
        Js.Num(2),
        Js.Str(write[ClusterMemberUp](r))
      )

    case r: ClusterMemberUnreachable =>
      Js.Arr(
        Js.Num(3),
        Js.Str(write[ClusterMemberUnreachable](r))
      )

    case r: ClusterMemberRemoved =>
      Js.Arr(
        Js.Num(4),
        Js.Str(write[ClusterMemberRemoved](r))
      )

    case r: ClusterUnjoin =>
      Js.Arr(
        Js.Num(5),
        Js.Str(write[ClusterUnjoin](r))
      )

    case other =>  Js.Str("Json error " + other)
  }


  implicit  val clusterProtocolReader = Reader[ClusterProtocol] {

    case Js.Arr(Js.Num(1), Js.Str(v)) =>
      read[CurrentClusterStateInitial](v)

    case Js.Arr(Js.Num(2), Js.Str(v)) =>
      read[ClusterMemberUp](v)

    case Js.Arr(Js.Num(3), Js.Str(v)) =>
      read[ClusterMemberUnreachable](v)

    case Js.Arr(Js.Num(4), Js.Str(v)) =>
      read[ClusterMemberRemoved](v)

    case Js.Arr(Js.Num(5), Js.Str(v)) =>
      read[ClusterUnjoin](v)
  }


  implicit val nodeStateProtocolWrite = Writer[NodeState] {
    case Up =>
      Js.Arr(Js.Num(1))

    case Unreachable =>
      Js.Arr(Js.Num(2))

    case Removed =>
      Js.Arr(Js.Num(3))

    case Exited =>
      Js.Arr(Js.Num(4))
  }

  implicit val nodeStateProtocolRead = Reader[NodeState] {
    case Js.Arr(Js.Num(1)) => Up
    case Js.Arr(Js.Num(2)) => Unreachable
    case Js.Arr(Js.Num(3)) => Removed
    case Js.Arr(Js.Num(4)) => Exited

  }



}
