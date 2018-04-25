import sbt._
import sbt.Keys._

object BuildConfig {
  object Dependencies {
    val testDeps = Seq(
      "org.scalatest" %% "scalatest" % versions.scalatest,
      "org.mockito" % "mockito-all" % versions.mockito,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % versions.scalacheckShapeless
    ).map(_ % "test")

    def awsServiceDep(service: String, version: String = versions.aws): ModuleID = {
      "com.amazonaws" % s"aws-java-sdk-$service" % version
    }
  }

  object Revision {
    lazy val version = System.getProperty("version", "1.0-SNAPSHOT")
  }

  object versions {
    val mockito = "1.10.19"
    val scalatest = "3.0.1"
    val scalacheckShapeless = "1.1.6"

    lazy val shapeless = "2.3.3"

    lazy val aws = "1.11.217"
  }

  def commonSettings() = {
    Seq(
      organization := "com.chrisbenincasa",

      version := BuildConfig.Revision.version,

      resolvers += Resolver.sonatypeRepo("releases"),

      scalaVersion := "2.12.5",

      crossScalaVersions := Seq("2.11.11", scalaVersion.value),

      libraryDependencies ++= Seq(
        "com.chuusai" %% "shapeless" % versions.shapeless
      ) ++ Dependencies.testDeps,

      scalacOptions ++= Seq(
        "-deprecation",
        "-encoding", "UTF-8",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-language:postfixOps",
        "-language:experimental.macros",
        "-unchecked",
        "-Ywarn-nullary-unit",
        "-Xfatal-warnings",
        "-Ywarn-dead-code",
        "-Xfuture"
      ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => Seq("-Xlint:-unused")
        case _ => Seq("-Xlint")
      }),

      scalacOptions in (Compile, doc) := scalacOptions.value.filterNot(_ == "-Xfatal-warnings"),
      scalacOptions in (Compile, doc) += "-no-java-comments"
    ) ++ Publishing.publishSettings
  }
}
