name := "clarifai-scala"

version := "1.0.0"

scalaVersion := "2.11.1"

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used...
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value ++ Seq(
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
	"org.scalaj" %% "scalaj-http" % "2.3.0")
    case _ =>
      libraryDependencies.value :+ "org.scalaj" %% "scalaj-http" % "2.3.0"
  }
}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/vic317yeh/clarifai-scala</url>
  <licenses>
    <license>
      <name>MIT License</name>
      <url>https://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:vic317yeh/clarifai-scala</url>
    <connection>scm:git:git@github.com:vic317yeh/clarifai-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>vic317yeh</id>
      <name>Vic Yeh</name>
      <url>https://github.com/vic317yeh</url>
    </developer>
  </developers>
)
