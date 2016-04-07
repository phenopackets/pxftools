enablePlugins(JavaAppPackaging)

organization  := "org.phenopackets"

name          := "pxftools"

version       := "0.1"

scalaVersion  := "2.11.8"

mainClass in Compile := Some("org.phenopackets.pxftools.Main")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.mavenLocal

javaOptions += "-Xmx4G"

libraryDependencies ++= {
  Seq(
    "org.phenopackets"            %  "phenopackets-api"     % "0.0.3",
    "org.backuity.clist"          %% "clist-core"           % "2.0.2",
    "org.backuity.clist"          %% "clist-macros"         % "2.0.2" % "provided",
    "org.apache.directory.studio" % "org.apache.commons.io" % "2.4"
  )
}
