lazy val common = project enablePlugins ScalaJSPlugin disablePlugins AssemblyPlugin settings (
  libraryDependencies += "me.chrons" %%% "boopickle" % "1.1.0"
)

lazy val frontend = project enablePlugins ScalaJSPlugin  disablePlugins AssemblyPlugin dependsOn common settings (
  libraryDependencies ++= List(
    "org.scala-js" %%% "scalajs-dom" % "0.8.1",
    "com.lihaoyi"  %%% "utest"       % "0.3.1" % "test"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  // auto-detect main and create launcher
  persistLauncher in Compile := true,
  persistLauncher in Test := false,
  // Use node instead of Rhino
  scalaJSStage in Global := FastOptStage,
  // and pull dom from phantom-js for tests
  jsDependencies += RuntimeDOM,
  // build fat-js, package deps
  skip in packageJSDependencies := false
)

lazy val backend = project dependsOn common settings (
  libraryDependencies ++= List(
    "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
    "de.knutwalker"     %% "typed-actors"           % "1.1.0"
  ),
  (resourceGenerators in Compile) <+= (
    fastOptJS in Compile in frontend,
    packageScalaJSLauncher in Compile in frontend
  ).map((f1, f2) ⇒ List(f1.data, f2.data)),
  watchSources <++= (watchSources in frontend),
  Revolver.settings,
  (assembledMappings in assembly) <+= (fullOptJS in Compile in frontend).map(f ⇒ sbtassembly.MappingSet(None, Vector((f.data, f.data.getName)))),
  assemblyJarName in assembly := s"${name.value}",
  assemblyOutputPath in assembly := baseDirectory.value / ".." / (assemblyJarName in assembly).value,
  assemblyOption in assembly ~= (_.copy(prependShellScript = Some(Seq("#!/usr/bin/env sh", """exec java -Xms256m -Xmx256m -client -Dapp.use-full-opt=true -jar "$0" "$@"""")))),
  assemblyMergeStrategy in assembly := {
    case "JS_DEPENDENCIES" ⇒ MergeStrategy.concat
    case x                 ⇒ (assemblyMergeStrategy in assembly).value.apply(x)
  }
)

lazy val talk = project in file(".") aggregate (common, frontend, backend)