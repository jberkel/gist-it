import sbt._
import Keys._
import AndroidKeys._
import Github._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    version := "0.1.4",
    versionCode := 5,
    organization := "com.zegoggles",
    scalaVersion := "2.9.1"
  )

  val androidSettings =
    settings ++
    Seq (
      platformName := "android-10"
    )

  val androidProjectSettings =
    androidSettings ++
    AndroidProject.androidSettings

  val androidFullProjectSettings =
    androidProjectSettings ++
    TypedResources.settings ++
    AndroidMarketPublish.settings ++
    AndroidManifestGenerator.settings ++
    Github.settings
}

object AndroidBuild extends Build {
  // MainProject
  lazy val app = Project (
    "gist-it",
    file("."),
    settings = General.androidFullProjectSettings ++ Seq (
      keyalias in Android := "jberkel",
      libraryDependencies ++= Seq(
        "org.acra" % "acra" % "4.2.3"
        //"com.github.jbrechtel" %% "robospecs" % "0.1-SNAPSHOT" % "test"
      ),
      compileOrder := CompileOrder.JavaThenScala,
      useProguard in Android := true,
      githubRepo  in Android := "gist-it",
      resolvers ++= Seq(
        MavenRepository("acra release repository", "http://acra.googlecode.com/svn/repository/releases"),
        MavenRepository("robospecs snapshots", "http://jbrechtel.github.com/repo/snapshots"),
        MavenRepository("scala tools snapshots", "http://scala-tools.org/repo-snapshots")
      )
    ) ++ AndroidInstall.settings
  )
}
