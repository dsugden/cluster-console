import sbt._

object ResolverSettings {

  lazy val resolvers = Seq(
    "rediscala" at "http://dl.bintray.com/etaty/maven",
    "bintray/meetup" at "http://dl.bintray.com/meetup/maven")
}

