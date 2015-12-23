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

    lazy val jna = "net.java.dev.jna" % "jna" % "3.2.7" % "provided"
    // lazy val opencv = "org.opencv" % "opencv" % "2.4.11"

    def libSrc(base: File) = Seq(
        base / "lejos" / "dbusjava" / "src",
        base / "lejos" / "ev3classes" / "src"
    )

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
        id = "line-follower-ev3",
        base = file("ev3"),
        settings = coreDefaultSettings ++ basicSettings ++ assemblySettings ++ tasks
    ) settings(
        libraryDependencies ++= Seq(jna),
        unmanagedSourceDirectories in Compile ++= libSrc(baseDirectory.value / ".."),
        unmanagedBase := baseDirectory.value / ".." / "lib",
        exportJars := true,
        assemblyJarName in assembly := "line-follower-ev3.jar",
        mainClass in assembly := Some("io.github.acidghost.robotics.LineFollower"),
        packageOptions in assembly += Package.ManifestAttributes(
            "Class-Path" -> "/home/root/lejos/libjna/usr/share/java/jna.jar"
        )
    )

    lazy val pc: Project = Project(
        id = "line-follower-server",
        base = file("server"),
        settings = coreDefaultSettings ++ basicSettings ++ assemblySettings ++ tasks
    ) settings(
        exportJars := true,
        assemblyJarName in assembly := "line-follower-server.jar",
        mainClass in assembly := Some("io.github.acidghost.robotics.LineFollowerServer")
    )

}
