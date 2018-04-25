import sbt._
import BuildConfig.Dependencies

lazy val commonSettings = BuildConfig.commonSettings()

lazy val general = project.in(file("general")).
  settings(commonSettings).
  settings(
    Seq(
      name := "general-geons"
    )
  )

lazy val aws = project.in(file("aws")).
  settings(commonSettings).
  settings(
    Seq(
      name := "aws-geons",
      libraryDependencies ++= Seq(
        Dependencies.awsServiceDep("dynamodb")
      )
    )
  )

lazy val geons = project.in(file(".")).
  settings(commonSettings).
  settings(
    Seq(
      name := "geons",
      aggregate in update := false
    )
  ).
  aggregate(aws, general)


lazy val showVersion = taskKey[Unit]("Show version")

showVersion := {
  println(version.value)
}

// custom alias to hook in any other custom commands
addCommandAlias("build", "; compile")
