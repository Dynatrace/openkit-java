# Dynatrace OpenKit - Java Example

The following document provides an in depth overview, how OpenKit can be used from
developer's point of view. It explains the usage of all the API methods.

## Obtaining an OpenKit Instance

Depending on the backend a new OpenKit instance can be obtained by using either `DynatraceOpenKitBuilder` 
or `AppMonOpenKitBuilder`. Despite from this, the developer does not need to distinguish between 
different backend systems.

### Dynatrace

For Dynatrace SaaS and Dynatrace Managed the `DynatraceOpenKitBuilder` is used to build new OpenKit instances. 

```java
String applicationID = "application-id";
long deviceID = 42;
String endpointURL = "https://tenantid.beaconurl.com/mbeacon";

OpenKit openKit = new DynatraceOpenKitBuilder(endpointURL, applicationID, deviceID).build();
```

* The `endpointURL` denotes the Dynatrace endpoint OpenKit communicates with and 
  is shown when creating the application in Dynatrace. The endpoint URL can be found 
  in the settings page of the custom application in Dynatrace.
* The `applicationID` parameter is the unique identifier of the application in Dynatrace Saas. The
  application's id can be found in the settings page of the custom application in Dynatrace.
* The `deviceID` is a unique identifier, which might be used to uniquely identify a device.

:grey_exclamation: For Dynatrace Managed the endpoint URL looks a bit different.

### AppMon

An OpenKit instance for AppMon can be obtained by using the `AppMonOpenKitBuilder`.

```java
String applicationName = "My OpenKit application";
long deviceID = 42;
String endpointURL = "https://beaconurl.com/dynaTraceMonitor";

// by default verbose logging is disabled
OpenKit openKit = new AppMonOpenKitBuilder(endpointURL, applicationName, deviceID).build();
```

* The `endpointURL` denotes the AppMon endpoint OpenKit communicates with.
* The `applicationName` parameter is the application's name in AppMon and is also used as the application's id.
* The `deviceID` is a unique identifier, which might be used to uniquely identify a device.

### Optional Configuration

In addition to the mandatory parameters described above, the builder provides additional methods to further 
customize OpenKit. This includes device specific information like operating system, manufacturer, or model id. 

| Method Name                           | Description                                                           | Default Value |
| -------------                         | -------------                                                         | ---------- |
| `withApplicationVersion`              | sets the application version                                          | `"2.2.0"` |
| `withOperatingSystem`                 | sets the operating system name                                        | `"OpenKit 2.2.0"` |
| `withManufacturer`                    | sets the manufacturer                                                 | `"Dynatrace"` |
| `withModelID`                         | sets the model id                                                     | `"OpenKitDevice"` |
| `withDataCollectionLevel`             | sets the data collection level                                        | `DataCollectionLevel.USER_BEHAVIOR` |
| `withCrashReportingLevel`             | sets the crash reporting level                                        | `CrashReportingLevel.OPT_IN_CRASHES` |
| `withBeaconCacheMaxRecordAge`         | sets the maximum age of an entry in the beacon cache in milliseconds  | 1 h 45 min |
| `withBeaconCacheLowerMemoryBoundary`  | sets the lower memory boundary of the beacon cache in bytes           | 80 MB |
| `withBeaconCacheUpperMemoryBoundary`  | sets the upper memory boundary of the beacon cache in bytes          | 100 MB |
| `withTrustManager`                    | sets a custom `SSLTrustManager` instance, replacing the builtin default one.<br>Details are described in section [SSL/TLS Security in OpenKit](#ssltls-security-in-openkit). | `SSLStrictTrustManager` |
| `enableVerbose`                       | *Deprecated*, use `withLogLevel` instead.<br>Enables extended log output for OpenKit if the default logger is used.<br>Is equivalent to `withLogLevel(LogLevel.DEBUG)`.  | `false` |
| `withLogLevel`                        | sets the default log level if the default logger is used              | `LogLevel.WARN` |
| `withLogger`                          | sets a custom logger, replacing the builtin default one.<br>Details are described in section [Logging](#logging). | `DefaultLogger` |


:grey_exclamation: Please refer to the the JavaDoc for more information regarding possible configuration values.

## SSL/TLS Security in OpenKit

All OpenKit communication to the backend happens via HTTPS (TLS/SSL based on Java Framework support).
By default OpenKit expects valid server certificates.
However it is possible, if really needed, to bypass TLS/SSL certificate validation. This can be achieved by
passing an implementation of `SSLTrustManager` by calling the `withTrustManager` on the builder.

:warning: We do **NOT** recommend bypassing TLS/SSL server certificate validation, since this allows
man-in-the-middle attacks.

## Logging

By default, OpenKit uses a logger implementation that logs to stdout. If the default logger is used, the desired
minimum log level can be set by calling `withLogLevel` in the builder, and only messages with the same or higher 
priorities are logged.

A custom logger can be set by calling `withLogger` in the builder. When a custom logger is used, a call to 
`withLogLevel` or `enableVerbose` has no effect. In that case, debug and info logs are logged depending on the values returned 
in `isDebugEnabled` and `isInfoEnabled`.

## Initializing OpenKit

When obtaining an OpenKit instance from the OpenKit builder the instance starts an automatic 
initialization phase. By default, initialization is performed asynchronously. 

There might be situations when a developer wants to ensure that initialization is completed before proceeding with 
the program logic. For example, short-lived applications where a valid init and shutdown cannot be guaranteed. In
such a case `waitForInitCompletion` can be used.

```java
// wait until the OpenKit instance is fully initialized
boolean success = openKit.waitForInitCompletion();
```

:grey_exclamation: Please refer to the Javadoc for additional information.

The method `waitForInitCompletion` blocks the calling thread until OpenKit is initialized. In case
of misconfiguration this might block the calling thread indefinitely. The return value
indicates whether the OpenKit instance has been initialized or `shutdown` has been called meanwhile.    
An overloaded method exists to wait a given amount of time for OpenKit to initialize as shown in the
following example.
```java
// wait up to 10 seconds for OpenKit to complete initialization
long timeoutInMilliseconds = 10 * 1000;
boolean success = openKit.waitForInitCompletion(timeoutInMilliseconds);
```

The method returns `false` in case the timeout expired or `shutdown` has been invoked in the meantime
and `true` to indicate successful initialization.  

To verify if OpenKit has been initialized, use the `isInitialized` method as shown in the example below.
```java
boolean isInitialized = openKit.isInitialized();
if (isInitialized) {
    System.out.println("OpenKit is initialized");
} else {
    System.out.println("OpenKit is not yet initialized");
}
```

## Creating a Session

After setting application version and device information, which is not mandatory, but might be useful,
a `Session` can be created by invoking the `createSession` method.  
There are two `createSession` methods:
1. Taking an IP address as string argument, which might be a valid IPv4 or IPv6 address.
If the argument is not a valid IP address a reasonable default value is used.
2. An overload taking no arguments. In this case the IP which communicates with the server is assigned
on the server.

The example shows how to create sessions.
```java
// create a session and pass an IP address
String clientIPAddress = "12.34.56.78";
Session sessionWithArgument = openKit.createSession(clientIPAddress);

// create a session and let the IP be assigned on the server side
Session sessionWithoutArgument = openKit.createSession();
```

## Identify User

Users can be identified by calling `identifyUser` on a `Session` instance. This enables you to search and 
filter specific user sessions and analyze individual user behavior over time in the backend.

```java
session.identifyUser("jane.doe@example.com");
```

## Finishing a Session

When a `Session` is no longer needed, it should be ended by invoking the `end` method.  
Although all open sessions are automatically ended when OpenKit is shut down (see "Terminating the OpenKit instance")
it's highly recommended to end sessions which are no longer in use manually.
```java
session.end();
session = null; // not needed, just used to indicate that the session is no longer valid.
```

## Reporting a Crash

Unexpected application crashes can be reported via a `Session` by invoking the `reportCrash` method.  
The example below shows how an exception might be reported.

```java
    private static int div(int numerator, int denominator) {
        return numerator / denominator;
    }

    public static void divWithCrash() {
        int numerator = 5;
        int denominator = 0;
        try {
            System.out.println("Got: " + div(numerator, denominator));
        } catch (Exception e) {
            String errorName = e.getClass().getName();
            String reason = e.getMessage();
            String stacktrace = getStackTraceAsString(e); // get the stacktrace as string, similar as e.printStackTrace()
            // and now report the application crash via the session
            session.reportCrash(errorName, reason, stacktrace);
        }
    }
```

Alternatively the `reportCrash(Throwable)` overloaded method can be used, which is provided for convenience.
The example below shows how to report a `Throwable` as crash.

```java
    private static int div(int numerator, int denominator) {
        return numerator / denominator;
    }

    public static void divWithCrash() {
        int numerator = 5;
        int denominator = 0;
        try {
            System.out.println("Got: " + div(numerator, denominator));
        } catch (Exception e) {
            // report the caught Exception as crash
            session.reportCrash(e);
        }
    }
```

## Starting a RootAction

As mentioned in the [README](#../README.md) root actions and actions are hierarchical named events, where
a `RootAction` represents the first hierarchy level. A `RootAction` can have child actions (`Action`) and
is created from a `Session` as shown in the example below.
```java
String rootActionName = "rootActionName";
RootAction rootAction = session.enterAction(rootActionName);
```

Since `RootAction` extends the `Action` interface all further methods are the same for both interfaces, except
for creating child actions, which can only be done with a `RootAction`.

## Entering a Child Action

To start a child `Action` from a previously started `RootAction` use the `enterAction` method from
`RootAction`, as demonstrated below.

```java
String childActionName = "childActionName";
Action childAction = rootAction.enterAction(childActionName);
```

## Leaving Actions

To leave an `Action` simply use the `leaveAction` method. The method returns the parent action or `null`
if it has no parent.

```java
Action parentAction = action.leave(); // returns the appropriate RootAction
Action parent = parentAction.leave(); // will always return null
```

## Report Named Event

To report a named event use the `reportEvent` method on `Action`.
```java
String eventName = "eventName";
action.reportEvent(eventName);

// also report on the RootAction
rootAction.reportEvent(eventName);
```

## Report Key-Value Pairs

Key-value pairs can also be reported via an `Action` as shown in the example below.
Overloaded methods exist for the following value types:
* int
* long
* double
* String
```java
// first report an int value
String keyIntType = "intType";
int valueInt = 42;
action.reportValue(keyIntType, valueInt);

// let's also report a long value 
String keyLongType = "longType";
long valueLong = Long.MAX_VALUE;
action.reportValue(keyLongType, valueLong);

// then let's report a double value
String keyDoubleType = "doubleType";
double valueDouble = 3.141592653589793;
action.reportValue(keyDoubleType, valueDouble);

// and also a string value
String keyStringType = "stringType";
String valueString = "The quick brown fox jumps over the lazy dog";
action.reportValue(keyStringType, valueString);
```

## Report an Error

An `Action` has the possibility to report an error with a given 
name and error code.  
The code fragment below shows how.
```java
String errorName = "Unknown Error";
int errorCode = 42;

action.reportError(errorName, errorCode);
```

Errors can also be reported with the method
`Action.reportError(String errorName, String causeName, String causeDescription, String causeStackTrace)`, where 
* `errorName` is the name of the reported error
* `causeName` is an optional short name of the cause, typically an `Exception` class name
* `causeDescription` is an optional short description of the cause, typically `Exception.getMessage()`
* `causeStackTrace` is an optional stack trace of the cause

The fragment below shows how to report such an error.

```java
public void restrictedMethod() {
    if (!isUserAuthorized()) {
        // user is not authorized - report this as an error
        String errorName = "Authorization error";
        String causeName = "User not authorized";
        String causeDescription = "The current user is not authorized to call restrictedMethod.";
        String stackTrace = null; // no stack trace is reported

        action.reportError(errorName, causeName, causeDescription, stackTrace);

        return;
    }

    // ... further processing ...
}
```

It is also possible to report a caught exception as error. This is a convenience method for the
`Action.reportError(String, String, String, String)` method mentioned above.
The example below demonstrates how to report a `Throwable` as error.

```java
try {
    // call a method that is throwing an exception 
    callMethodThrowingException();
} catch(Exception caughtException) {
    // report the caught exception as error via OpenKit
    String errorName = "Unknown Error";
    action.reportError(errorName, caughtException);
}
``` 

## Tracing Web Requests

One of the most powerful OpenKit features is web request tracing. When the application starts a web
request (e.g. HTTP GET) a special tag can be attached to the header. This special header allows
Dynatrace SaaS/Dynatrace Managed/AppMon to correlate actions with a server side PurePath. 

An example is shown below.
```java
// create URL and open URLConnection
URL url = new URL("http://www.my-backend.com/api/v3/users");
URLConnection urlConnection = url.openConnection();

// create the WebRequestTracer
WebRequestTracer webRequestTracer = action.traceWebRequest(urlConnection);
webRequestTracer.start();

// consume data
BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
String inputLine;
while ((inputLine = in.readLine()) != null) {
    // TODO - do something usefule with response
}
in.close();

// stop web request tracing when done
webRequestTracer.stop(200);     // would use the HTTP response code normally.
```

If a third party lib is used for HTTP requests, the developer has the possibility to use an overloaded
`traceWebRequest` method, taking only the URL string as argument. However when using this overloaded
method the developer is responsible for adding the appropriate header field to the request.  
The field name can be obtained from `OpenKitConstants.WEBREQUEST_TAG_HEADER` and the field's value is obtained
from `getTag` method (see class `WebRequestTracer`).

```java
String url = "http://www.my-backend.com/api/v3/users";

// create the WebRequestTracer
WebRequestTracer webRequestTracer = action.traceWebRequest(url);

// this is the HTTP header name & value which needs to be added to the HTTP request.
String headerName = OpenKitConstants.WEBREQUEST_TAG_HEADER;
String headerValue = webRequestTracer.getTag();

webRequestTracer.start();

// perform the request here & do not forget to add the HTTP header

webRequestTracer.setBytesSent(12345);     // 12345 bytes sent
webRequestTracer.setBytesReceived(67890); // 67890 bytes received
webRequestTracer.stop(200);               // 200 was the response code
```


## Terminating the OpenKit Instance

When an OpenKit instance is no longer needed (e.g. the application using OpenKit is shut down), the previously
obtained instance can be cleared by invoking the `shutdown` method.  
Calling the `shutdown` method blocks the calling thread while the OpenKit flushes data which has not been
transmitted yet to the backend (Dynatrace SaaS/Dynatrace Managed/AppMon).  
Details are explained in [internals.md](internals.md)
