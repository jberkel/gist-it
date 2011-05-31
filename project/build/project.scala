import sbt._

trait Defaults extends BaseAndroidProject {
  def androidPlatformName = "android-8"
  override def skipProguard = false
}

class Parent(info: ProjectInfo) extends ParentProject(info) with IdeaProject {
  override def shouldCheckOutputDirectories = false
  override def updateAction = task { None }

  lazy val main  = project(".", "gist-it", new MainProject(_))
  lazy val tests = project("tests",  "tests", new TestProject(_), main)

  class MainProject(info: ProjectInfo) extends AndroidProject(info)
      with Defaults
      with MarketPublish
      with IdeaProject
      with TypedResources
      with posterous.Publish {

    override def compileOptions = super.compileOptions ++ Seq(Unchecked)
    // needed to get annotations to work
    override def compileOrder = CompileOrder.JavaThenScala

    val keyalias  = "change-me"

    val snapshots = "snapshots" at "http://scala-tools.org/repo-snapshots"
    val releases  = "releases" at "http://scala-tools.org/repo-releases"
    val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

    val robospecs = "com.github.jbrechtel" %% "robospecs" % "0.1-SNAPSHOT" % "test"
    val robospecsSnapshots  = "snapshots" at "http://jbrechtel.github.com/repo/snapshots"

    val acra = "org.acra" % "acra" % "4.2.2b-SNAPSHOT"

    def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
    override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
    override def typedResource = manifestPackage.split('.').foldLeft(managedScalaPath)( (p,s) => p/s ) / "TR.scala"
  }

  class TestProject(info: ProjectInfo) extends AndroidTestProject(info)
    with Defaults
    with IdeaProject
}
