name := "toycert"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.8",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.8",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.8.8",
  "org.glassfish.jersey.containers" % "jersey-container-servlet" % "2.25.1",
  "org.glassfish.jersey.containers" % "jersey-container-jetty-http" % "2.25.1",
  "org.glassfish.jersey.core" % "jersey-server" % "2.25.1",
  "org.eclipse.jetty" % "jetty-servlet" % "9.4.3.v20170317",
  "org.eclipse.jetty" % "jetty-server" % "9.4.3.v20170317",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.jdbi" % "jdbi" % "2.78",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.5.9",
  "org.mindrot" % "jbcrypt" % "0.4",
  "org.flywaydb" % "flyway-core" % "4.1.2",
  "com.zaxxer" % "HikariCP" % "2.6.1",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.56",
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.56",
  "net.jcazevedo" %% "moultingyaml" % "0.4.0",
  "com.mashape.unirest" % "unirest-java" % "1.4.9",
  "com.github.pathikrit" %% "better-files" % "3.0.0" % "test",
  "commons-io" % "commons-io" % "2.5" % "test",
  "org.scalatest" % "scalatest_2.12" % "3.0.1" % "test",
  "org.apache.httpcomponents" % "httpclient" % "4.3.6" % "test",
  "org.apache.httpcomponents" % "httpasyncclient" % "4.0.2" % "test",  
  "org.apache.httpcomponents" % "httpmime" % "4.3.6" % "test",
  "org.json" % "json" % "20140107" % "test"
)

fork in run := true

baseDirectory in run := file(".")

