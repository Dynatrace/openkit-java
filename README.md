# Dynatrace OpenKit - Java Reference Implementation

## What is the OpenKit?

The OpenKit provides an easy and lightweight way to get insights into applications with Dynatrace/AppMon by instrumenting the source code of those applications.

It is best suited for applications running separated from their backend and communicating via HTTP, like rich-client-applications, embedded devices, terminals, and so on.

The big advantages of the OpenKit are that it's designed to
* be as easy-to-use as possible
* be as dependency-free as possible (no third party libraries or Dynatrace/AppMon Agent needed)
* be easily portable to other languages and platforms

This repository contains the reference implementation in pure Java. Other implementations are listed as follows:
* .NET: https://github.com/Dynatrace/openkit-dotnet/

## What you can do with the OpenKit
* Create Sessions and User Actions
* Report values, events, errors and crashes
* Tag web requests to server-side PurePaths
* Use it together with Dynatrace or AppMon

## What you cannot do with the OpenKit
* Create server-side PurePaths (you have to use an ADK for that)

## Design Principles
* API should be as simple and easy-to-understand as possible
* Incorrect usage of the OpenKit should still lead to valid results, if possible
* In case of errors, the API should not throw exceptions, but only log those errors (in verbose mode)
* No usage of third-party libraries, should run without any dependencies
* Avoid usage of newest Java APIs, should be running on older Java VMs, too
* Avoid usage of too much Java-specific APIs to allow rather easy porting to other languages

## Prerequisites

### Running the OpenKit
* Java Runtime Environment (JRE) 6, 7 or 8

### Building the Source/Generating the JavaDoc
* Java Development Kit (JDK) 6, 7 or 8
  * Environment Variable JAVA_HOME set to JDK install directory
  * Paths of `java[6|7|8].boot.classpath` in `build.xml` adapted to JDK install directory
* Apache Ant 1.8.x+
  * Environment Variable PATH pointing to Ant binary

### (Unit-)Testing the OpenKit
* Java Runtime Environment (JRE) 6, 7 or 8
* JUnit 4.x+ (incl. Hamcrest Core 1.3+)

## Building the Source

```
$ ant [build_java6|build_java7|build_java8]         # default is to build for Java 6, 7 & 8
```

The built jar file(s) `openkit-<version>-java<java_version>.jar` will be located in the `dist` directory.

## Generating the JavaDoc

```
$ generate_javadoc
```

The generated javadoc will be located in the `javadoc` directory.

## General Concepts
* TBD

## Known Current Limitations

* problem with SSL keysize > 1024 for Diffie-Hellman (used by Dynatrace) in Java 6 (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7044060)
  * fixed in Java 6u171, which is only available via Oracle support (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8182231)
* ignored configs: capture lifecycle, crash reporting, error reporting, session timeout
* it's only possible to have one OpenKit instance running as providers are static

## TODOs

* add multiple time syncs for Dynatrace
* move providers from static to instance (multiple OpenKits -> multiple providers)
* prevent re-entrances e.g. of startup/shutdown
* HTTPS certificate verification
* HTTP optimizations (reuse connection, pool http client?)
* provide simple samples to get started as markdown
* mobile sampling
