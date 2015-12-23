import sbt.Defaults.coreDefaultSettings
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.AssemblyPlugin.assemblySettings

object Build extends Build {

    lazy val basicSettings = Seq(
        scalaVersion := "2.11.7",
        javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
        scalacOptions := Seq("-encoding", "UTF-8", "-unchecked", "-deprecation", "-feature", "-target:jvm-1.7")
    )

    lazy val ev3DepsSettings = Seq(
        libraryDependencies ++= Seq(jna),
        scalaSource in Compile := baseDirectory.value / "src"
    )

    lazy val jna = "net.java.dev.jna" % "jna" % "3.2.7" % "provided"

    lazy val tasks = Seq(installDepsTask, deployTask)

    val installDeps = TaskKey[Unit]("install-deps", "Install unmanaged dependencies like leJOS")
    val installDepsTask = installDeps := { "./install.sh" ! }

    val deploy = TaskKey[Unit]("deploy", "Deploy to EV3")
    val deployTask = deploy := { "scp ev3/target/scala-2.11/line-follower-ev3.jar ev3:/home/lejos/programs/" ! }

    lazy val root = Project(
        id = "line-follower",
        base = file("."),
        settings = coreDefaultSettings ++ basicSettings ++ assemblySettings ++ tasks
    ) dependsOn(ev3, pc) aggregate(ev3, pc)

    lazy val ev3: Project = Project(
        id = "ev3",
        base = file("ev3"),
        settings = coreDefaultSettings ++ basicSettings ++ assemblySettings
    ) settings(
        unmanagedBase := baseDirectory.value / ".." / "lib",
        exportJars := true,
        assemblyJarName in assembly := "line-follower-ev3.jar",
        mainClass in assembly := Some("io.github.acidghost.robotics.LineFollower"),
        packageOptions in assembly += Package.ManifestAttributes(
            "Class-Path" -> "/home/root/lejos/libjna/usr/share/java/jna.jar"
        )
    ) dependsOn ev3classes aggregate(ev3classes, dbusjava)

    lazy val ev3classes = Project(
        id = "ev3-classes",
        base = file("lejos/ev3classes"),
        settings = coreDefaultSettings ++ basicSettings ++ assemblySettings ++ ev3DepsSettings
    ) dependsOn dbusjava

    lazy val dbusjava = Project(
        id = "dbus-java",
        base = file("lejos/dbusjava"),
        settings = coreDefaultSettings ++ basicSettings ++ assemblySettings ++ ev3DepsSettings
    )

    lazy val pc: Project = Project(
        id = "server",
        base = file("server"),
        settings = coreDefaultSettings ++ basicSettings ++ assemblySettings
    ) settings(
        exportJars := true,
        assemblyJarName in assembly := "line-follower-server.jar",
        mainClass in assembly := Some("io.github.acidghost.robotics.LineFollowerServer")
    )

}
