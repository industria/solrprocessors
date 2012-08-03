name := "solrprocessors"

version := "2.0"

description := "Solr processors for use in the update request processor chain."

organization := "dk.industria.solr.processors"

scalacOptions += "-deprecation"

scalaVersion := "2.9.2"

libraryDependencies += "org.apache.solr" % "solr-core" % "3.5.0" % "compile"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1" % "compile"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.3" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.8" % "test"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

pomExtra := <xml:group>
  <name>solrprocessors</name>
  <description>Solr processors for use in the update request processor chain.</description>
  <inceptionYear>2011</inceptionYear>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
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
