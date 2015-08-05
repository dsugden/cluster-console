import com.lihaoyi.workbench.Plugin._
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.SbtWeb
import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport._
import com.typesafe.sbt.packager.archetypes._
import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import spray.revolver.RevolverPlugin.Revolver


name := "cluster-console"

version := "1.1"

scalaVersion := "2.11.7"



// a special crossProject for configuring a JS/JVM/shared structure
// root project aggregating the JS and JVM projects
lazy val root = project.in(file(".")).
  aggregate(js, jvm).
  settings(
    name := "cluster-console",
    version := Settings.version,
    commands += ReleaseCmd,
    publish := {},
    publishLocal := {}
  )

// Command for building a release
lazy val ReleaseCmd = Command.command("release") {
  state => "set productionBuild in js := true" ::
    "set elideOptions in js := Seq(\"-Xelide-below\", \"WARNING\")" ::
    "sharedProjectJS/test" ::
    "sharedProjectJS/fullOptJS" ::
    "sharedProjectJS/packageJSDependencies" ::
    "sharedProjectJVM/test" ::
    "sharedProjectJVM/stage" ::
    "set productionBuild in js := false" ::
    state
}
val sharedSrcDir = "shared"

val productionBuild = settingKey[Boolean]("Build for production")
val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")
val copyWebJarResources = taskKey[Unit]("Copy resources from WebJars")

// a special crossProject for configuring a JS/JVM/shared structure
lazy val sharedProject = crossProject.in(file("."))
  .settings(
    name := Settings.name,
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    sourceDirectory in Assets := baseDirectory.value / "src" / "main" / "assets",
    LessKeys.compress in Assets := true,
    libraryDependencies ++= Settings.sharedDependencies.value,
    copyWebJarResources := {
      // copy the compiled CSS
      val s = streams.value
      s.log("Copying webjar resources")
      val compiledCss = webTarget.value / "less" / "main" / "stylesheets"
      val targetDir = (classDirectory in Compile).value / "web"
      IO.createDirectory(targetDir / "stylesheets")
      IO.copyDirectory(compiledCss, targetDir / "stylesheets")
      // copy font-awesome fonts from WebJar
      val fonts = (webModuleDirectory in Assets).value / "webjars" / "lib" / "font-awesome" / "fonts"
      IO.createDirectory(targetDir / "fonts")
      IO.copyDirectory(fonts, targetDir / "fonts")
    },
    // run the copy after compile/assets but before managed resources
    copyWebJarResources <<= copyWebJarResources dependsOn(compile in Compile, assets in Compile),
    managedResources in Compile <<= (managedResources in Compile) dependsOn copyWebJarResources
  )

  // set up settings specific to the JVM project
  .jvmSettings(Revolver.settings: _*)

  .jvmSettings(
    libraryDependencies ++= Settings.jvmDependencies.value,
    // copy resources from the "shared" project
    unmanagedResourceDirectories in Compile += file(".") / sharedSrcDir / "src" / "main" / "resources",
    unmanagedResourceDirectories in Test += file(".") / sharedSrcDir / "src" / "test" / "resources",

    javaOptions in Revolver.reStart ++= Settings.jvmRuntimeOptions,

    // configure a specific port for debugging, so you can easily debug multiple projects at the same time if necessary
    Revolver.enableDebugging(port = 5111, suspend = false)
  )

  // set up settings specific to the JS project
  .jsSettings(workbenchSettings: _*)
  .jsSettings(
    libraryDependencies ++= Settings.scalajsDependencies.value,
    // by default we do development build
    productionBuild := false,
    elideOptions := Seq(),
    scalacOptions ++= elideOptions.value,
    // scalacOptions in (Compile, fullOptJS) ++= Seq("-Xelide-below", "WARNING"),
    // select JS dependencies according to build setting
    jsDependencies ++= {if (!productionBuild.value) Settings.jsDependencies.value else Settings.jsDependenciesProduction.value},
    jsDependencies ++= Settings.jsDependencies.value,
    // RuntimeDOM is needed for tests
    jsDependencies += RuntimeDOM % "test",
    scalacOptions ++= Seq({
      val a = js.base.toURI.toString.replaceFirst("[^/]+/?$", "")
      s"-P:scalajs:mapSourceURI:$a->/srcmaps/"
    }),

    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,

    // copy resources from the "shared" project
    unmanagedResourceDirectories in Compile += file(".") / sharedSrcDir / "src" / "main" / "resources",
    unmanagedResourceDirectories in Test += file(".") / sharedSrcDir / "src" / "test" / "resources",

    // use uTest framework for tests
    testFrameworks += new TestFramework("utest.runner.Framework"),

    // define where the JS-only application will be hosted by the Workbench plugin
    localUrl :=("localhost", 13131),
    refreshBrowsers <<= refreshBrowsers.triggeredBy(fastOptJS in Compile),
    bootSnippet := "ClusterConsoleApp().main();"
  )

// configure a specific directory for scalajs output
val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

// make all JS builds use the output dir defined later
lazy val js2jvmSettings = Seq(fastOptJS, fullOptJS, packageJSDependencies) map { packageJSKey =>
  crossTarget in(js, Compile, packageJSKey) := scalajsOutputDir.value
}

// instantiate the JS project for SBT with some additional settings
lazy val js: Project = sharedProject.js.settings(
  fastOptJS in Compile := {
    // make a copy of the produced JS-file (and source maps) under the js project as well,
    // because the original goes under the jvm project
    // NOTE: this is only done for fastOptJS, not for fullOptJS
    val base = (fastOptJS in Compile).value
    IO.copyFile(base.data, (classDirectory in Compile).value / "web" / "js" / base.data.getName)
    IO.copyFile(base.data, (classDirectory in Compile).value / "web" / "js" / (base.data.getName + ".map"))
    base
  },

  packageJSDependencies in Compile := {
    // make a copy of the produced jsdeps file under the js project as well,
    // because the original goes under the jvm project
    val base = (packageJSDependencies in Compile).value
    IO.copyFile(base, (classDirectory in Compile).value / "web" / "js" / base.getName)
    base
  }
).settings(scalariformSettings).enablePlugins(SbtWeb)


// instantiate the JVM project for SBT with some additional settings
lazy val jvm: Project = sharedProject.jvm.settings(js2jvmSettings: _*)
  .settings(scalariformSettings)
  .settings(
  // scala.js output is directed under "web/js" dir in the jvm project
  scalajsOutputDir := (classDirectory in Compile).value / "web" / "js",
  // set environment variables in the execute scripts
  NativePackagerKeys.batScriptExtraDefines += "set PRODUCTION_MODE=true",
  NativePackagerKeys.bashScriptExtraDefines += "export PRODUCTION_MODE=true",
  // reStart depends on running fastOptJS on the JS project
  Revolver.reStart <<= Revolver.reStart dependsOn (fastOptJS in(js, Compile))
).enablePlugins(SbtWeb).enablePlugins(JavaAppPackaging)


lazy val sampleCluster = (project in file("sampleCluster"))
  .settings(scalariformSettings)
  .settings(
    name := "samplecluster",
    version  := "1.0.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Settings.jvmDependencies.value
  ).enablePlugins(JavaServerAppPackaging)
