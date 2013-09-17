import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ledenadmin"
  val appVersion      = "1.2.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "net.sf.opencsv" % "opencsv" % "2.3",
    "mysql" % "mysql-connector-java" % "5.1.18",
    "com.feth" %% "play-easymail" % "0.2-SNAPSHOT",
    "be.objectify" %% "deadbolt-java" % "2.1-RC2",
    "commons-codec" % "commons-codec" % "1.8"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),

      resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns)
  )

}
