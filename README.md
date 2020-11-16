# Dynatrace OpenKit - Java Reference Implementation

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/Dynatrace/openkit-java.svg?branch=release%2F2.1)](https://travis-ci.org/Dynatrace/openkit-java)
[![Coverage Status](https://coveralls.io/repos/github/Dynatrace/openkit-java/badge.svg?branch=release%2F2.1)](https://coveralls.io/github/Dynatrace/openkit-java?branch=release%2F2.1)

:information_source: We changed the default branch name to `main`. You can find the necessary steps to update your local clone on [Scott Hanselman's Blog](https://www.hanselman.com/blog/EasilyRenameYourGitDefaultBranchFromMasterToMain.aspx).  
We encourage you to rename the default branch in your forks too.

## What is the OpenKit?

The OpenKit provides an easy and lightweight way to get insights into applications with Dynatrace/AppMon by instrumenting the source code of those applications.

It is best suited for applications running separated from their backend and communicating via HTTP, like rich-client-applications, embedded devices, terminals, and so on.

The big advantages of the OpenKit are that it's designed to
* be as easy-to-use as possible
* be as dependency-free as possible (no third party libraries or Dynatrace/AppMon Agent needed)
* be easily portable to other languages and platforms

This repository contains the reference implementation in pure Java. Other implementations are listed as follows:
* .NET: https://github.com/Dynatrace/openkit-dotnet/
* C/C++: https://github.com/Dynatrace/openkit-native/
* JavaScript: https://github.com/Dynatrace/openkit-js

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

## General Remarks
* All non binary files within the repository are formatted with UNIX style (LF) line endings.

## Getting started
* [Installing and updating OpenKit Java][installing]
* [General concepts][concepts]
* [Detailed example][example]
* [Contributing to OpenKit Java][contributing]
* [Supported versions][supported_versions]
* [Upgrade guide][upgrade_guide]

## Advanced topics
* [Building OpenKit Java][building]
* [OpenKit Java internals][internals]

## Known Current Limitations
* problem with SSL keysize > 1024 for Diffie-Hellman (used by Dynatrace) in Java 6 (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7044060)
  * fixed in Java 6u171, which is only available via Oracle support (http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8182231)
 
[installing]: ./docs/installing.md
[concepts]: ./docs/concepts.md
[example]: ./docs/example.md
[contributing]: ./CONTRIBUTING.md
[supported_versions]: ./docs/supported_versions.md
[building]: ./docs/building.md
[internals]: ./docs/internals.md
[upgrade_guide]: ./docs/upgrade_guide.md