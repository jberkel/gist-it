import sbt._

trait Defaults extends BaseAndroidProject {
  def androidPlatformName = "android-7"
  override def skipProguard = true
}

class Parent(info: ProjectInfo) extends ParentProject(info) {
  override def shouldCheckOutputDirectories = false
  override def updateAction = task { None }

  lazy val main  = project(".", "gist", new MainProject(_))
  lazy val tests = project("tests",  "tests", new TestProject(_), main)


  class MainProject(info: ProjectInfo) extends AndroidProject(info)
      with Defaults
      with MarketPublish
      with TypedResources {

    val keyalias  = "change-me"

    val robospecs = "com.github.jbrechtel" %% "robospecs" % "0.1-SNAPSHOT" % "test"
    val robospecsSnapshots  = "snapshots" at "http://jbrechtel.github.com/repo/snapshots"
    val snapshots = "snapshots" at "http://scala-tools.org/repo-snapshots"
    val releases  = "releases" at "http://scala-tools.org/repo-releases"
  }

  class TestProject(info: ProjectInfo) extends AndroidTestProject(info) with Defaults
}
