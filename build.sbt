import ReleaseTransformations._
import sbtcrossproject.CrossPlugin.autoImport.crossProject

val commonSettings = Seq(
  organization := "com.github.cb372",
  publishTo := sonatypePublishTo.value,
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  pomIncludeRepository := { _ => false },
  publishMavenStyle := true,
  licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://cb372.github.io/cats-retry/")),
  developers := List(
    Developer(
      id    = "cb372",
      name  = "Chris Birchall",
      email = "chris.birchall@gmail.com",
      url   = url("https://github.com/cb372")
    )
  )
)

val moduleSettings = commonSettings ++ Seq(
  moduleName := s"cats-retry-${name.value}",
  scalacOptions ++= Seq(
    "-Xfuture",
    "-Ywarn-dead-code",
    "-Ywarn-unused",
    "-deprecation",
    "-encoding", "UTF-8",
    "-language:higherKinds",
    "-unchecked",
  ),
  scalacOptions in (Test, compile) += "-Ypartial-unification",
  scalafmtOnCompile := true
)

val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("modules/core"))
  .settings(moduleSettings)
  .settings(
    libraryDependencies ++= Seq(
        "org.typelevel" %%% "cats-core" % "1.4.0",
        "org.scalatest" %%% "scalatest" % "3.0.5" % Test,
        "org.scalacheck" %%% "scalacheck" % "1.14.0" % Test
      )
    )
val coreJVM = core.jvm
val coreJS = core.js

val catsEffect = crossProject(JVMPlatform, JSPlatform)
  .in(file("modules/cats-effect"))
  .jvmConfigure(_.dependsOn(coreJVM))
  .jsConfigure(_.dependsOn(coreJS))
  .settings(moduleSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "1.0.0",
      "org.scalatest" %%% "scalatest" % "3.0.5" % Test,
      "org.scalacheck" %%% "scalacheck" % "1.14.0" % Test
    )
  )
val catsEffectJVM = catsEffect.jvm
val catsEffectJS = catsEffect.js

val monix = crossProject(JVMPlatform, JSPlatform)
  .in(file("modules/monix"))
  .jvmConfigure(_.dependsOn(coreJVM))
  .jsConfigure(_.dependsOn(coreJS))
  .settings(moduleSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.monix" %%% "monix" % "3.0.0-RC1",
      "org.scalatest" %%% "scalatest" % "3.0.5" % Test,
      "org.scalacheck" %%% "scalacheck" % "1.14.0" % Test
    )
  )
val monixJVM = monix.jvm
val monixJS = monix.js

val docs = project.in(file("modules/docs"))
  .dependsOn(coreJVM, catsEffectJVM, monixJVM)
  .enablePlugins(MicrositesPlugin)
  .settings(moduleSettings)
  .settings(
    scalacOptions -= "-Ywarn-dead-code",
    scalacOptions -= "-Ywarn-unused"
  )
  .settings(
    publishArtifact := false,
    micrositeName := "cats-retry",
    micrositeAuthor := "Chris Birchall",
    micrositeDescription := "cats-retry",
    micrositeBaseUrl := "/cats-retry",
    micrositeDocumentationUrl := "/cats-retry/docs",
    micrositeHomepage := "https://github.com/cb372/cats-retry",
    micrositeGithubOwner := "cb372",
    micrositeGithubRepo := "cats-retry",
    micrositeGitterChannel := true,
    micrositeTwitterCreator := "@cbirchall",
    micrositeShareOnSocial := true
  )

val root = project.in(file("."))
  .aggregate(coreJVM, coreJS, catsEffectJVM, catsEffectJS, monixJVM, monixJS, docs)
  .settings(commonSettings)
  .settings(
    publishTo := sonatypePublishTo.value, // see https://github.com/sbt/sbt-release/issues/184
    publishArtifact := false,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    )
  )