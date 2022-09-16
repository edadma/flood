name := "flood"

version := "0.0.1"

versionScheme := Some("early-semver")

scalaVersion := "3.2.0"

enablePlugins(ScalaNativePlugin)

nativeLinkStubs := true

nativeMode := "debug"

nativeLinkingOptions := Seq(s"-L${baseDirectory.value}/native-lib")

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:existentials",
)

organization := "io.github.edadma"

githubOwner := "edadma"

githubRepository := name.value

Global / onChangedBuildSource := ReloadOnSourceChanges

resolvers += Resolver.githubPackages("edadma")

resolvers += Resolver.githubPackages("spritzsn")

licenses := Seq("ISC" -> url("https://opensource.org/licenses/ISC"))

homepage := Some(url("https://github.com/edadma/" + name.value))

//libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.13" % "test"

libraryDependencies ++= Seq(
  "io.github.spritzsn" %%% "libuv" % "0.0.27",
  "io.github.edadma" %%% "iup" % "0.1.1",
  "io.github.spritzsn" %%% "async" % "0.0.13",
)

publishMavenStyle := true

Test / publishArtifact := false
