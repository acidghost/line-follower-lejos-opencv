import sbt.Defaults.coreDefaultSettings
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.AssemblyPlugin.assemblySettings

object Build extends Build {

    lazy val basicSettings = Seq(
        scalaVersion := "2.11.7",
        // licenses := Seq(("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
        javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
        scalacOptions := Seq("-encoding", "UTF-8", "-unchecked", "-deprecation", "-feature", "-target:jvm-1.7"),
        libraryDependencies += jna
    )

    lazy val jna = "net.java.dev.jna" % "jna" % "3.2.7" % "provided"

    def libSrc(base: File) = {
        Seq(
            base / "lejos" / "dbusjava" / "src",
            base / "lejos" / "ev3classes" / "src"
        )
    }

    val installDeps = TaskKey[Unit]("install-deps", "Install unmanaged dependencies like leJOS")
    val installDepsTaks = installDeps := { "./install.sh" ! }

    val deploy = TaskKey[Unit]("deploy", "Deploy to EV3")
    val deployTask = deploy := { "scp target/scala-2.11/line-follower.jar ev3:/home/lejos/programs/" ! }

    val tasks = Seq(installDepsTaks, deployTask)

    lazy val root = Project(
        "line-follower",
        file("."),
        settings = coreDefaultSettings ++ basicSettings ++ assemblySettings ++ tasks
    ).settings(
        unmanagedSourceDirectories in Compile ++= libSrc(baseDirectory.value),
        exportJars := true,
        assemblyJarName in assembly := "line-follower.jar",
        assemblyExcludedJars in assembly <<= unmanagedJars in Compile,
        mainClass in assembly := Some("io.github.acidghost.robotics.LineFollower"),
        packageOptions in assembly += Package.ManifestAttributes(
            "Class-Path" -> "/home/root/lejos/libjna/usr/share/java/jna.jar"
        )
    )

}
