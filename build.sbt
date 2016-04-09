enablePlugins(JavaAppPackaging)

organization  := "org.phenopackets"

name          := "pxftools"

version       := "0.1"

scalaVersion  := "2.11.8"

mainClass in Compile := Some("org.phenopackets.pxftools.Main")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers += Resolver.mavenLocal

javaOptions += "-Xmx4G"

libraryDependencies ++= {
  Seq(
    "org.phenopackets"            %  "phenopackets-api"      % "0.0.4-SNAPSHOT",
    "org.backuity.clist"          %% "clist-core"            % "2.0.2",
    "org.backuity.clist"          %% "clist-macros"          % "2.0.2" % "provided",
    "org.apache.directory.studio" %  "org.apache.commons.io" % "2.4",
    "org.specs2"                  %% "specs2"                % "3.7" % "test"
  )
}
