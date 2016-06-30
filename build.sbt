enablePlugins(JavaAppPackaging)

organization  := "org.phenopackets"

name          := "pxftools"

version       := "0.0.2-SNAPSHOT"

scalaVersion  := "2.11.8"

mainClass in Compile := Some("org.phenopackets.pxftools.Main")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers += Resolver.mavenLocal

javaOptions += "-Xmx4G"

libraryDependencies ++= {
  Seq(
    "org.phenopackets"            %  "phenopackets-api"      % "0.0.4",
    "org.backuity.clist"          %% "clist-core"            % "2.0.2",
    "org.backuity.clist"          %% "clist-macros"          % "2.0.2" % "provided",
    "net.sourceforge.owlapi"      %  "owlapi-distribution"   % "4.2.5",
    "org.apache.jena"             %  "apache-jena-libs"      % "3.1.0",
    "com.github.jsonld-java"      %  "jsonld-java"           % "0.8.3",
    "org.apache.directory.studio" %  "org.apache.commons.io" % "2.4",
    "org.scalaz"                  %% "scalaz-core"           % "7.2.1",
    "com.typesafe.scala-logging"  %% "scala-logging"         % "3.4.0",
    "ch.qos.logback"              %  "logback-classic"       % "1.1.7",
    "org.codehaus.groovy"         %  "groovy-all"            % "2.4.6",
    "org.specs2"                  %% "specs2"                % "3.7" % Test
  )
}
