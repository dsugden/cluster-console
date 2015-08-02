package clusterconsole.http

sealed trait NodeState
case object Up extends NodeState
case object Unreachable extends NodeState
case object Removed extends NodeState
case object Exited extends NodeState
