# Dynatrace OpenKit - Java Reference Implementation

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/Dynatrace/openkit-java.svg?branch=master)](https://travis-ci.org/Dynatrace/openkit-java)
[![Coverage Status](https://coveralls.io/repos/github/Dynatrace/openkit-java/badge.svg)](https://coveralls.io/github/Dynatrace/openkit-java)

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
* Trace web requests to server-side PurePaths
* Tag Sessions with a user tag
* Use it together with Dynatrace or AppMon

## What you cannot do with the OpenKit
* Create server-side PurePaths (this functionality is provided by [OneAgent SDKs](https://github.com/Dynatrace/OneAgent-SDK))
* Create metrics (use the [Custom network devices & metrics API](https://www.dynatrace.com/support/help/dynatrace-api/timeseries/what-does-the-custom-network-devices-and-metrics-api-provide/) to report metrics)

## Design Principles
* API should be as simple and easy-to-understand as possible
* Incorrect usage of the OpenKit should still lead to valid results, if possible
* In case of errors, the API should not throw exceptions, but only log those errors (in verbose mode)
* No usage of third-party libraries, should run without any dependencies
* Avoid usage of newest Java APIs, should be running on older Java VMs, too
* Avoid usage of too much Java-specific APIs to allow rather easy porting to other languages
* Design reentrant APIs and document them

## Prerequisites

### Running the OpenKit
* Java Runtime Environment (JRE) 6, 7 or 8

### Building the Source/Generating the JavaDoc
* Java Development Kit (JDK) 6, 7 or 8
  * Environment Variable JAVA_HOME set to JDK install directory

### (Unit-)Testing the OpenKit
* Java Runtime Environment (JRE) 6, 7 or 8  
  Dependencies for testing (JUnit, Hamcrest, Mockito) are managed by Gradle.

## Building the Source

Assuming you are in OpenKit's top level directory  

```shell
gradlew jar
```

The built jar file(s) `openkit-<version>-java<java_version>.jar` will be located in the `build/dist` directory.

## Generating the JavaDoc

```shell
gradlew javadoc
```

The generated javadoc will be located in the `build/docs/javadoc` directory.

## General Concepts

In this part the concepts used throughout OpenKit are explained. A short sample how to use OpenKit is
also provided. For detailed code samples have a look into [example.md](docs/example.md).

### DynatraceOpenKitBuilder / AppMonOpenKitBuilder
A `DynatraceOpenKitBuilder`/`AppMonOpenKitBuilder` instance is responsible for setting 
application relevant information, e.g. the application's version and device specific information, and to create
an `OpenKit` instance.

### OpenKit

The OpenKit is responsible for creating user sessions (see Session).
  
Although it would be possible to have multiple `OpenKit` instances connected to the same endpoint
(Dynatrace/AppMon) within one process, there should be one unique instance. `OpenKit` is designed to be
thread safe and therefore the instance can be shared among threads.  

On application shutdown, `shutdown()` needs to be called on the OpenKit instance.

### Session

A `Session` represents kind of a user session, similar to a browser session in a web application.
However the application developer is free to choose how to treat a `Session`.  
The `Session` is used to create `RootAction` instances and report application crashes.  

When a `Session` is no longer required, it's highly recommended to end it, using the `Session.end()` method. 

### RootAction and Action

The `RootAction` and `Action` are named hierarchical nodes for timing and attaching further details.
A `RootAction` is created from the `Session` and it can create `Action` instances. Both, `RootAction` and
`Action`, provide the possibility to attach key-value pairs, named events and errors, and can be used 
for tracing web requests.

### WebRequestTracer

When the application developer wants to trace a web request, which is served by a service 
instrumented by Dynatrace, a `WebRequestTracer` should be used, which can be
requested from a `Session` or an `Action`.  

### Named Events

A named `Event` is attached to an `Action` and contains a name.

### Key-Value Pairs

For an `Action` key-value pairs can also be reported. The key is always a String
and the value may be an Integer (int), a floating point (double) or a String.

### Errors & Crashes

Errors are a way to report an erroneous condition on an `Action`.  
Crashes are used to report (unhandled) exceptions on a `Session`.

### Identify Users

OpenKit enables you to tag sessions with unique user tags. The user tag is a String 
that allows to uniquely identify a single user.

## Example

This small example provides a rough overview how OpenKit can be used.  
Detailed explanation is available in [example.md](docs/example.md).

```java
String applicationName = "My OpenKit application";
String applicationID = "application-id";
long deviceID = 42;
String endpointURL = "https://tenantid.beaconurl.com/mbeacon";

OpenKit openKit = new DynatraceOpenKitBuilder(endpointURL, applicationID, deviceID)
    .withApplicationName(applicationName)
    .withApplicationVersion("1.0.0.0")
    .withOperatingSystem("Windows 10")
    .withManufacturer("MyCompany")
    .withModelID("MyModelID")
    .build();

String clientIP = "8.8.8.8";
Session session = openKit.createSession(clientIP);

session.identifyUser("jane.doe@example.com");

String rootActionName = "rootActionName";
RootAction rootAction = session.enterAction(rootActionName);

String childActionName = "childAction";
Action childAction = rootAction.enterAction(childActionName);

childAction.leaveAction();
rootAction.leaveAction();
session.end();
openKit.shutdown();
``` 

## Known Current Limitations
* problem with SSL keysize > 1024 for Diffie-Hellman (used by Dynatrace) in Java 6 (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7044060)
  * fixed in Java 6u171, which is only available via Oracle support (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8182231)
