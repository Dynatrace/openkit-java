# General Concepts

In this part the concepts used throughout OpenKit are explained. A short sample how to use OpenKit is
also provided. For detailed code samples have a look into [example.md][example].

## DynatraceOpenKitBuilder / AppMonOpenKitBuilder
A `DynatraceOpenKitBuilder`/`AppMonOpenKitBuilder` instance is responsible for setting 
application relevant information, e.g. the application's version and device specific information, and to create
an `OpenKit` instance.

## OpenKit

The OpenKit is responsible for creating user sessions (see [Session](#session)).
  
Although it would be possible to have multiple `OpenKit` instances connected to the same endpoint
(Dynatrace/AppMon) within one process, there should be one unique instance. `OpenKit` is designed to be
thread safe and therefore the instance can be shared among threads.  

On application shutdown, `shutdown()` needs to be called on the OpenKit instance.

## Session

A `Session` represents kind of a user session, similar to a browser session in a web application.
However the application developer is free to choose how to treat a `Session`.  
The `Session` is used to create `RootAction` instances and report application crashes.  

When a `Session` is no longer required, it's highly recommended to end it, using the `Session.end()` method. 

## RootAction and Action

The `RootAction` and `Action` are named hierarchical nodes for timing and attaching further details.
A `RootAction` is created from the `Session` and it can create `Action` instances. Both, `RootAction` and
`Action`, provide the possibility to attach key-value pairs, named events and errors, and can be used 
for tracing web requests.

## WebRequestTracer

When the application developer wants to trace a web request, which is served by a service 
instrumented by Dynatrace, a `WebRequestTracer` should be used, which can be
requested from a `Session` or an `Action`.  

## Named Events

A named `Event` is attached to an `Action` and contains a name.

## Key-Value Pairs

For an `Action` key-value pairs can also be reported. The key is always a String
and the value may be an Integer (int), a floating point (double) or a String.

## Errors & Crashes

Errors are a way to report an erroneous condition on an `Action`.  
Crashes are used to report (unhandled) exceptions on a `Session`.

## Identify Users

OpenKit enables you to tag sessions with unique user tags. The user tag is a String 
that allows to uniquely identify a single user.


## Example

This small example provides a rough overview how OpenKit can be used.  
Detailed explanation is available in [example.md][example].

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

[example]: (./example.md)
