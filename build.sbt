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
