import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._


/**
 * Defines settings for the projects:
 */
object Settings {

  object versions {
    val akka = "2.3.12"
    val akkaHttp = "1.0-RC4"
    val akkaLog4j = "0.2.0"
    val bootstrap = "3.3.2"
    val d3 = "3.5.5-1"
    val jQuery = "1.11.1"
    val log4j = "2.3"
    val scala = "2.11.7"
    val scalaTest = "2.2.5"
    val logBack = "1.1.2"
    val scalaLogging = "3.1.0"
    val config = "1.3.0"
    val react = "0.12.1"
    val scalacheck = "1.12.2"
    val scalaCSS = "0.2.0"
    val scalajsDom = "0.8.0"
    val scalajsReact = "0.9.0"
    val scalaRx = "0.2.8"
    val scalaTags = "0.5.2"
    val scalaz = "7.1.2"
    val upickle = "0.2.8"
    val utest = "0.3.1"
  }


  val name = "cluster-console"
  val version = "0.1"
  val scalaVersion = "2.11.7"
  val scalacOptions = Seq("-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation", "-language:postfixOps")
  //  val javacOptions  = Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation", "-Xlint:-options"),


  /** Set some basic options when running the project with Revolver */
  val jvmRuntimeOptions = Seq(
    "-Xmx1G"
  )



  val sharedDependencies = Def.setting(Seq(
    "com.lihaoyi" %%% "autowire" % "0.2.5",
    "com.lihaoyi" %%% "upickle" % "0.3.4",
    "com.lihaoyi" %%% "utest" % "0.3.1",
    "org.webjars" % "font-awesome" % "4.3.0-1" % Provided,
    "org.webjars" % "bootstrap" % versions.bootstrap % Provided
  ))

  /** Dependencies only used by the JVM project */
  val jvmDependencies = Def.setting(Seq(
    "com.typesafe.akka" %% "akka-actor" % versions.akka,
    "com.typesafe.akka" %% "akka-cluster" % versions.akka,
    "com.typesafe.akka" %% "akka-contrib" % versions.akka,
    "com.typesafe.akka" %% "akka-http-experimental" % versions.akkaHttp,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % versions.akkaHttp,
    "com.typesafe.akka" % "akka-stream-experimental_2.11" % versions.akkaHttp,
    "com.typesafe.akka" %% "akka-slf4j" % versions.akka,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % versions.akka,
    "com.typesafe.akka" %% "akka-testkit" % versions.akka,
    "org.apache.logging.log4j" % "log4j-core" % versions.log4j,
    "org.scalatest" %% "scalatest" % versions.scalaTest % "test",
    "ch.qos.logback" % "logback-classic" % versions.logBack,
    "com.typesafe.scala-logging" %% "scala-logging" % versions.scalaLogging,
    "com.typesafe" % "config" % versions.config,
    "org.scalaz" %% "scalaz-core" % versions.scalaz,
    "com.lihaoyi" %% "scalatags" % versions.scalaTags
  ))

  //  /** Dependencies only used by the JS project (note the use of %%% instead of %%) */
  val scalajsDependencies = Def.setting(Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % versions.scalajsReact,
    "com.github.japgolly.scalajs-react" %%% "extra" % versions.scalajsReact,
    "com.github.japgolly.scalacss" %%% "ext-react" % versions.scalaCSS,
    "org.scala-js" %%% "scalajs-dom" % "0.8.0",
    "com.lihaoyi" %%% "scalarx" % "0.2.8"
  ))

  val jsDependencies = Def.setting(Seq(
    "org.webjars" % "react" % versions.react / "react-with-addons.js" commonJSName "React",
    "org.webjars" % "jquery" % versions.jQuery / "jquery.js",
    "org.webjars" % "bootstrap" % versions.bootstrap / "bootstrap.js" dependsOn "jquery.js",
    "org.webjars" % "d3js" % versions.d3 / "d3.js"
  ))
  /** Same dependecies, but for production build, using minified versions */
//  val jsDependenciesProduction = Def.setting(Seq(
//    "org.webjars" % "react" % versions.react / "react-with-addons.min.js" commonJSName "React",
//    "org.webjars" % "jquery" % versions.jQuery / "jquery.min.js",
//    "org.webjars" % "bootstrap" % versions.bootstrap / "bootstrap.min.js" dependsOn "jquery.min.js",
//    "org.webjars" % "d3js" % versions.d3 / "d3.min.js" commonJSName "d3"
//  ))


  val jsDependenciesProduction = Def.setting(Seq(
    "org.webjars" % "react" % versions.react / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
    "org.webjars" % "jquery" % versions.jQuery / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "bootstrap" % versions.bootstrap / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js",
    "org.webjars" % "d3js" % versions.d3 / "d3.js" minified "d3.min.js" commonJSName "d3"
  ))


}

