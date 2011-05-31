import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val android   = "org.scala-tools.sbt" % "sbt-android-plugin" % "0.5.2-SNAPSHOT"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.7"
  val sbtIdea   = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.5.0-SNAPSHOT"
}
