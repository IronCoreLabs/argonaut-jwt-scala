organization := "com.ironcorelabs"
name := "argonaut-jwt-scala"
scalaVersion := "2.12.0"

com.typesafe.sbt.SbtScalariform.scalariformSettings

scalacOptions := Seq(
      "-deprecation",
      "-encoding", "UTF-8", // yes, this is 2 args
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ypartial-unification",
      "-Xfuture",
      "-language:higherKinds"
  )

libraryDependencies ++= Seq(
  "io.argonaut" %% "argonaut" % "6.2-RC2",
  "com.pauldijou" %% "jwt-json-common" % "0.12.0",
  "org.scodec" %% "scodec-bits" % "1.1.2"
  ) ++ Seq(
  "org.scalatest" %% "scalatest" % "3.0.0"
  ).map(_ % "test")

coverageMinimum := 80
coverageFailOnMinimum := true
