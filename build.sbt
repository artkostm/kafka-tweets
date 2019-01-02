lazy val root = (project in file("."))
  .aggregate(tweetPublisher, job)

lazy val tweetPublisher = (project in file("tweet-publisher"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(Settings.default("2.11.12"))
  .settings(libraryDependencies ++= Dependencies.mainP)
  .settings(libraryDependencies ++= Dependencies.unitTestsP)
  .settings(libraryDependencies ++= Dependencies.itTestsP)

lazy val job = (project in file("job"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(Settings.default("2.11.12"))
  .settings(libraryDependencies ++= Dependencies.mainJ)
  .settings(libraryDependencies ++= Dependencies.unitTestsJ)
  .settings(libraryDependencies ++= Dependencies.itTestsJ)
  .settings(
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )
  .dependsOn(tweetPublisher % IntegrationTest)
