# microBean Narayana JTA Weld SE Integration

[![Build Status](https://travis-ci.org/microbean/microbean-narayana-jta-weld-se.svg?branch=master)](https://travis-ci.org/microbean/microbean-narayana-jta-weld-se)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.microbean/microbean-narayana-jta-weld-se/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.microbean/microbean-narayana-jta-weld-se)

The microBean Narayana JTA Weld SE Integration project integrates the
[Narayana transaction engine][narayana-jta] into [CDI 2.0 SE
environments][cdi].

# Installation

To install the microBean Narayana JTA Weld SE Integration project,
ensure that it and its dependencies are present on the classpath at
runtime.  In Maven, your dependency stanza should look like this:

    <dependency>
      <groupId>org.microbean</groupId>
      <artifactId>microbean-narayana-jta-weld-se</artifactId>
      <!-- See http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.microbean%22%20AND%20a%3A%22microbean-narayana-jta-weld-se%22 for available releases. -->
      <version>0.2.1</version>
      <type>jar</type>
      <scope>runtime</scope>
    </dependency>
    
Releases are [available in Maven Central][maven-central].  Snapshots
are available in [Sonatype Snapshots][sonatype-snapshots].

[narayana-jta]: http://narayana.io/
[cdi]: http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#part_2
[maven-central]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.microbean%22%20AND%20a%3A%22microbean-narayana-jta-weld-se%22
[sonatype-snapshots]: https://oss.sonatype.org/content/repositories/snapshots/org/microbean/microbean-narayana-jta-weld-se/

