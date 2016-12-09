name := "bewl"

version := "1.0"

scalaVersion := "2.12.1"

scalacOptions ++= Seq("-feature", "-deprecation", "-Xexperimental")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.12.1",
  "junit" % "junit" % "4.12" % "test" exclude("org.scala-lang.modules", "scala-xml_2.12"),
  "org.scalatest" %% "scalatest" % "3.0.1" exclude("org.scala-lang.modules", "scala-xml_2.12")
) map {
  _ withSources() withJavadoc()
}



