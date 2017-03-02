organization := "com.ironcorelabs"

licenses := Seq("Apache-2.0" -> url("http://www.opensource.org/licenses/Apache-2.0"))

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

useGpg := true

publishTo := {
  val nexus = "s3://maven.ironcorelabs.com"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "/snapshots")
  else
    Some("releases"  at nexus + "/releases")
}

pomExtra := (
  <url>https://github.com/IronCoreLabs/argonaut-jwt-scala</url>
    <licenses>
      <license>
        <name>Apache2</name>
        <url>http://www.opensource.org/licenses/Apache-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:IronCoreLabs/argonaut-jwt-scala.git</url>
      <connection>scm:git@github.com:IronCoreLabs/argonaut-jwt-scala.git</connection>
    </scm>
    <developers>
      {
      Seq(
        ("coltfred", "Colt Frederickson")
      ).map {
        case (id, name) =>
          <developer>
            <id>{id}</id>
            <name>{name}</name>
            <url>http://github.com/{id}</url>
          </developer>
      }
    }
    </developers>
  )
