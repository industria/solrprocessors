name := "solrprocessors"

version := "4.8"

description := "Solr processors for use in the update request processor chain."

organization := "dk.industria.solr.processors"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-optimise", "-feature", "-Xlint", "-language:postfixOps")

libraryDependencies += "org.apache.solr" % "solr-core" % "8.11.1" % "compile"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "compile"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.7.2" % "test"

resolvers += "Public online Restlet repository" at "https://maven.restlet.org/"

pomExtra := <xml:group>
  <name>solrprocessors</name>
  <description>Solr processors for use in the update request processor chain.</description>
  <inceptionYear>2011</inceptionYear>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>manual</distribution>
    </license>
  </licenses>
  <url>https://github.com/industria/solrprocessors/</url>
  <scm>
    <connection>scm:git://github.com/industria/solrprocessors.git</connection>
    <developerConnection>scm:git@github.com:industria/solrprocessors.git</developerConnection>
    <tag>HEAD</tag>
    <url>https://github.com/industria/solrprocessors/</url>
  </scm>
  <issueManagement>
    <system>Github Issues</system>
    <url>https://github.com/industria/solrprocessors/issues/</url>
  </issueManagement>
</xml:group>
