lazy val common = project enablePlugins ScalaJSPlugin disablePlugins AssemblyPlugin settings (
  libraryDependencies += "me.chrons" %%% "boopickle" % "1.1.0"
)

lazy val frontend = project enablePlugins ScalaJSPlugin  disablePlugins AssemblyPlugin dependsOn common settings (
  libraryDependencies ++= List(
    "org.scala-js" %%% "scalajs-dom" % "0.8.1",
    "com.lihaoyi"  %%% "utest"       % "0.3.1" % "test"
  ),
  // can use webjars for js dependencies (bower on JVM)
  jsDependencies += "org.webjars" % "toastr" % "2.1.1" / "2.1.1/toastr.js",
  // and pull dom from phantom-js for tests
  jsDependencies in Test += RuntimeDOM,
  testFrameworks += new TestFramework("utest.runner.Framework"),
  // auto-detect main and create launcher
  persistLauncher in Compile := true,
  persistLauncher in Test := false,
  // Use node instead of Rhino
  scalaJSStage in Global := FastOptStage,
  // build fat-js, package deps
  skip in packageJSDependencies := false
)

lazy val backend = project dependsOn common settings (
  libraryDependencies ++= List(
    "org.webjars"        % "toastr"                 % "2.1.1",
    "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
    "de.knutwalker"     %% "typed-actors"           % "1.1.0"
  ),
  (resourceGenerators in Compile) <+= (
    fastOptJS in Compile in frontend,
    packageScalaJSLauncher in Compile in frontend,
    packageJSDependencies in Compile in frontend
  ).map((f1, f2, f3) ⇒ Seq(f1.data, f2.data, f3)),
  watchSources <++= (watchSources in frontend),
  Revolver.settings,

  // assembly file settings
  (assembledMappings in assembly) <+= (
    fullOptJS in Compile in frontend,
    packageMinifiedJSDependencies in Compile in frontend
  ).map((f1, f2) ⇒ sbtassembly.MappingSet(None, Vector(
    (f1.data, f1.data.getName),
    (f2, f2.getName)
  ))),
  assemblyJarName in assembly := s"${name.value}",
  assemblyOutputPath in assembly := baseDirectory.value / ".." / (assemblyJarName in assembly).value,
  assemblyOption in assembly ~= (_.copy(prependShellScript = Some(Seq("#!/usr/bin/env sh", """exec java -Xms256m -Xmx256m -client -Dapp.use-full-opt=true -Dakka.actor.debug.receive=off -Dakka.loglevel=INFO -jar "$0" "$@"""")))),
  assemblyMergeStrategy in assembly := {
    case "JS_DEPENDENCIES" ⇒ MergeStrategy.concat
    case x                 ⇒ (assemblyMergeStrategy in assembly).value.apply(x)
  }
)

lazy val talk = project in file(".") aggregate (common, frontend, backend)
