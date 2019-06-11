organization := "com.ironcorelabs"
name := "argonaut-jwt-scala"
scalaVersion := "2.12.3"
crossScalaVersions := Seq("2.11.12", "2.12.4")

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
      "-Xfuture",
      "-language:higherKinds"
  )

libraryDependencies ++= Seq(
  "io.argonaut" %% "argonaut" % "6.2.2",
  "com.pauldijou" %% "jwt-json-common" % "3.0.0",
  "org.scodec" %% "scodec-bits" % "1.1.11"
  ) ++ Seq(
  "org.scalatest" %% "scalatest" % "3.0.5"
  ).map(_ % "test")

// HACK: without these lines, the console is basically unusable,
// since all imports are reported as being unused (and then become
// fatal errors).
scalacOptions in (Compile, console) ~= { _.filterNot(_.startsWith("-Xlint")).filterNot(_.startsWith("-Ywarn")) }
scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

coverageMinimum := 80
coverageFailOnMinimum := true
