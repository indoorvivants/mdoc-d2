Global / excludeLintKeys += logManager

inThisBuild(
  List(
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := scalaBinaryVersion.value,
    organization := "com.indoorvivants",
    organizationName := "Anton Sviridov",
    homepage := Some(
      url("https://github.com/indoorvivants/mdoc-d2")
    ),
    startYear := Some(2023),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "keynmol",
        "Anton Sviridov",
        "velvetbaldmime@protonmail.com",
        url("https://blog.indoorvivants.com")
      )
    )
  )
)

val Versions = new {
  val Scala213 = "2.13.10"
  val Scala212 = "2.12.17"
  val Scala3 = "3.2.2"
  val scalaVersions = Seq(Scala3, Scala212, Scala213)

  val yank = "0.0.1"
  val mdoc = "2.3.7"
  val scalameta = "4.7.1"
}

lazy val munitSettings = Seq(
  libraryDependencies += {
    "org.scalameta" %% "munit" % "1.0.0-M7" % Test
  },
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val root = project
  .in(file("."))
  .aggregate(core.projectRefs: _*)
  .settings(publish / skip := true, publishLocal / skip := true)

lazy val core = projectMatrix
  .in(file("modules/core"))
  .settings(
    moduleName := "mdoc-d2",
    Test / scalacOptions ~= filterConsoleScalacOptions,
    libraryDependencies += "com.indoorvivants" %% "yank" % Versions.yank,
    libraryDependencies += "org.scalameta" %% "mdoc" % Versions.mdoc % "provided"
  )
  .settings(munitSettings)
  .jvmPlatform(Versions.scalaVersions)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "com.indoorvivants.mdoc_d2.internal",
    buildInfoOptions += BuildInfoOption.PackagePrivate,
    buildInfoKeys := Seq[BuildInfoKey](
      version,
      scalaVersion,
      scalaBinaryVersion
    )
  )

lazy val docs = project
  .in(file("target/.docs"))
  .settings(
    scalaVersion := Versions.Scala213,
    mdocVariables := Map(
      "VERSION" -> version.value
    ),
    publish / skip := true,
    publishLocal / skip := true
  )
  .dependsOn(core.jvm(Versions.Scala213))
  .enablePlugins(MdocPlugin)
  .settings(
    mdocVariables := Map("VERSION" -> "0.0.1")
  )

lazy val docsDrifted = taskKey[Boolean]("")
docsDrifted := {
  val readmeIn = (baseDirectory.value / "README.in.md").toString
  val generated =
    (docs / Compile / mdoc).toTask(s"").value

  val out = (docs / Compile / mdocOut).value / "README.in.md"

  val actualReadme = (ThisBuild / baseDirectory).value / "README.md"

  val renderedContents = IO.read(out)
  val actualContents = IO.read(actualReadme)

  renderedContents != actualContents
}

lazy val checkDocs = taskKey[Unit]("")
checkDocs := {

  val hasDrifted = docsDrifted.value

  if (hasDrifted) {
    throw new MessageOnlyException(
      "Docs have drifted! please run `updateDocs` in SBT to rectify"
    )
  }
}

lazy val updateDocs = taskKey[Unit]("")
updateDocs := {

  val hasDrifted = docsDrifted.value

  if (hasDrifted) {
    sLog.value.warn("README.md has drifted, overwriting it")

    val out = (docs / Compile / mdocOut).value / "README.in.md"

    val actualReadme = (ThisBuild / baseDirectory).value / "README.md"

    IO.copyFile(out, actualReadme)
  } else {

    sLog.value.info("README.md is up to date")
  }
}

val scalafixRules = Seq(
  "OrganizeImports",
  "DisableSyntax",
  "LeakingImplicitClassVal",
  "NoValInForComprehension"
).mkString(" ")

val CICommands = Seq(
  "clean",
  "checkDocs",
  "scalafmtCheckAll",
  "scalafmtSbtCheck",
  s"scalafix --check $scalafixRules",
  "headerCheck"
).mkString(";")

val PrepareCICommands = Seq(
  s"scalafix --rules $scalafixRules",
  "scalafmtAll",
  "scalafmtSbt",
  "headerCreate",
  "updateDocs"
).mkString(";")

addCommandAlias("ci", CICommands)

addCommandAlias("preCI", PrepareCICommands)
