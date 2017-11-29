# Dynatrace OpenKit - Java example

The following document shall provide an in depth overview, how OpenKit can be used from
developer's point of view. It explains the usage of all the API methods.

## Obtaining an OpenKit instance

OpenKit instances are obtained from the `OpenKitFactory` class.  
Depending on the used backend system (Dynatrace SaaS/Dynatrace Managed/AppMon), the factory provides 
different methods to create a new  OpenKit instance. Despite from this, the developer does not 
need to distinguish between different backend systems.

### Dynatrace SaaS
 
```java
String applicationName = "My OpenKit application";
String applicationID = "application-id";
long visitorID = 42;
String endpointURL = "https://tenantid.beaconurl.com";

// by default verbose logging is disabled
OpenKit openKit = OpenKitFactory.createDynatraceInstance(applicationName, applicationID, visitorID, endpointURL);
```

* The `applicationName` parameter is the application's name created before in Dynatrace SaaS.
* The `applicationID` parameter is the unique identifier of the application in Dynatrace Saas. The
application's id can be found in the settings page of the custom application in Dynatrace.
* The `visitorID` is a unique identifier, which might be used to uniquely identify a device.
* The `endpointURL` denotes the Dynatrace SaaS cluster endpoint OpenKit communicates with and 
  is shown when creating the application in Dynatrace SaaS.
The endpoint URL can be found in the settings page of the custom application in Dynatrace.

OpenKit provides extended log output by activating the verbose mode. This feature might come in quite handy during development,
therefore an overloaded method exists, where verbose mode can be enabled or disabled.  
To enable verbose mode, use the following example.

```java
String applicationName = "My OpenKit application";
String applicationID = "application-id";
long visitorID = 42;
String endpointURL = "https://tenantid.beaconurl.com";
boolean verbose = true;

// by default verbose logging is disabled
OpenKit openKit = OpenKitFactory.createDynatraceInstance(applicationName, applicationID, visitorID, endpointURL, verbose);
```

### Dynatrace Managed

An OpenKit instance for Dynatrace Managed can be obtained in a similar manner, as shown in the example below.
```java
String applicationName = "My OpenKit application";
String applicationID = "application-id";
long visitorID = 42;
String endpointURL = "https://tenantid.beaconurl.com";
String tenantID = "tenant-id";

// by default verbose logging is disabled
OpenKit openKit = OpenKitFactory.createDynatraceManagedInstance(applicationName, applicationID, visitorID, endpointURL, tenantID);
```

* The `applicationName` parameter is the application's name created before in Dynatrace Managed.
* The `applicationID` parameter is the unique identifier of the application in Dynatrace Managed. The
application's id can be found in the settings page of the custom application in Dynatrace.
* The `visitorID` is a unique identifier, which might be used to uniquely identify a device.
* The `endpointURL` denotes the Dynatrace Managed endpoint OpenKit communicates with. The endpoint URL can be found in 
the settings page of the custom application in Dynatrace.
* The `tenantID` is the tenant used by Dynatrace Managed.

Again an overloaded method exists to enable verbose logging, as shown below.
```java
String applicationName = "My OpenKit application";
String applicationID = "application-id";
long visitorID = 42;
String endpointURL = "https://beaconurl.com";
String tenantID = "tenant-id";
boolean verbose = true;

// by default verbose logging is disabled
OpenKit openKit = OpenKitFactory.createDynatraceManagedInstance(applicationName, applicationID, visitorID, endpointURL, tenantID, verbose);
```

### AppMon

The example below demonstrates how to connect an OpenKit application to an AppMon endpoint.
```java
String applicationName = "My OpenKit application";
long visitorID = 42;
String endpointURL = "https://beaconurl.com";

// by default verbose logging is disabled
OpenKit openKit = OpenKitFactory.createAppMonInstance(applicationName, visitorID, endpointURL);
```

* The `applicationName` parameter is the application's name in AppMon and is also used as the application's id.
* The `visitorID` is a unique identifier, which might be used to uniquely identify a device.
* The `endpointURL` denotes the AppMon endpoint OpenKit communicates with.

If verbose OpenKit logging output is wanted, an overloaded method can be used as demonstrated below.
```java
String applicationName = "My OpenKit application";
long visitorID = 42;
String endpointURL = "https://tenantid.beaconurl.com";
boolean verbose = true;

// by default verbose logging is disabled
OpenKit openKit = OpenKitFactory.createAppMonInstance(applicationName, visitorID, endpointURL, verbose);
```

## Initializing OpenKit

After the OpenKit instance is obtained, the `initialize` method must be called. Since initialization
happens asynchronously the application developer might want to wait until initialization completes, as
shown in the example below.

```java
// initialize previously obtained OpenKit instance
openKit.initialize();

// and wait until it's fully initialized
boolean success = openKit.waitForInitCompletion();
```

The method `waitForInitCompletion` blocks the calling thread until OpenKit is initialized. In case
of misconfiguration this might block the calling thread indefinitely. The return value
indicates whether the OpenKit instance has been initialized or `shutdown` has been called meanwhile.    
An overloaded method exists to wait a given amount of time for OpenKit to initialize as shown in the
following example.
```java
// initialize previously obtained OpenKit instance
openKit.initialize();

// wait 10 seconds for OpenKit
long timeoutInMilliseconds = 10 * 1000;
boolean success = openKit.waitForInitCompletion(timeoutInMilliseconds);
```

The method returns `false` in case the timeout expired or `shutdown` has been invoked in the mean time
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

## Providing further Application information

If multiple version's of the same applications are monitored by OpenKit, it's quite useful
to set the application's version in OpenKit.  
This can be achieved by calling
```java
String applicationVersion = "1.2.3.4";
openKit.setApplicationVersion(applicationVersion);
```

## Providing Device specific information

Sometimes it might also be quite useful to provide information about the device the application
is running on. The example below shows how to achieve this.
```java
// set operating system
String operatingSystem = "Custom OS";
openKit.getDevice().setOperatingSystem(operatingSystem);

// set device manufacturer
String deviceManufacturer = "ACME Inc.";
openKit.getDevice().setOperatingSystem(deviceManufacturer);

// set device/model identifier
String deviceID = "12-34-56-78-90";
openKit.getDevice().setModelID(deviceID);
```

## Creating a Session

After setting application version and device information, which is not mandatory, but might be useful,
a `Session` can be created by invoking the `createSession` method.  
There are two `createSession` method:
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

## Finishing a Session

When a `Session` is no longer needed, a Session should be ended by invoking the `end` method.  
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

## Starting a RootAction

As mentioned in the [README](#../README.md) root actions and actions are hierarchical named events, where
a `RootAction` represents the first hierarchy level. A `RootAction` can have child actions (`Action`) and
is created from a `Session` as shown in the example below.
```java
String rootActionName = "rootActionName";
RootAction rootAction = session.enterAction(rootActionName);
```

Since `RootAction` extends the `Action` all further methods are the same for both interfaces, except
for creating child actions, which can only be done with a `RootAction`.

## Entering a child Action

To start a child `Action` from a previously started `RootAction` use the `enterAction` method from
`RootAction`, as demonstrated below.

```java
String childActionName = "childActionName";
Action childAction = rootAction.enterAction(childActionName);
```

## Leaving Actions

To leave an `Action` simply use the `leave` method. The method returns the parent action or `null`
if it has no parent.

```java
Action parentAsAction = action.leave(); // returns the appropriate RootAction
Action parent = parentAsAction.leave(); // will always return null
```

## Report Named Event

To report a named event use the `reportEvent` method on `Action` (including of course `RootAction`).
```java
String eventName = "eventName";
action.reportEvent(eventName);

// also report on the RootAction
rootAction.reportEvent(eventName);
```

## Report key-value pairs

Key-value pairs can also be reported via an `Action` or a `RootAction` as shown in the example below.
Overloaded methods exist for the following value types:
* int
* double
* String
```java
// first report an int value
String keyIntType = "intType";
int valueInt = 42;
action.reportValue(keyIntType, valueInt);

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

An `Action` (including `RootAction`) also has the possibility to report an error with a given 
name, code and a reason. The code fragment below shows how.
```java
String errorName = "Unknown Error";
int errorCode = 42;
String reason = "Not sure what's going on here";

action.reportError(errorName, errorCode, reason);
```

## Tracing Web Requests

One of the most powerful OpenKit features is web request tracing. When the application starts a web
request (e.g. HTTP GET) a special tag can be attached to the header. This special header allows
Dynatrace SaaS/Dynatrace Managed/AppMon to correlate with a server side PurePath. 

An example is shown below.
```java
// create URL and open URLConnection
URL url = new URL("http://www.my-backend.com/api/v3/users");
URLConnection urlConnection = url.openConnection();

// create the WebRequestTracer
WebRequestTracer webRequestTracer = action.traceWebRequest(urlConnection);
webRequestTracer.startTiming();

// consume data
BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
String inputLine;
while ((inputLine = in.readLine()) != null) {
    // TODO - do something usefule with response
}
in.close();

// stop web request tracing when done
webRequestTracer.setResponseCode(200); // would use the HTTP response code normally.
webRequestTracer.stopTiming();
```

If a third party lib is used for HTTP requests, the developer has the possibility to use an overloaded
`traceWebRequest` method, taking only the URL string as argument. However when using this overloaded
method the developer is responsible for adding the appropriate header field to the request.  
The field name can be obtained from `OpenKit.WEBREQUEST_TAG_HEADER` and the field's value is obtained
from `getTag` method (see class `WebRequestTracer`).

```java
String url = "http://www.my-backend.com/api/v3/users";

// create the WebRequestTracer
WebRequestTracer webRequestTracer = action.traceWebRequest(url);

// this is the HTTP header name & value which needs to be added to the HTTP request.
String headerName = OpenKit.WEBREQUEST_TAG_HEADER;
String headerValue = webRequestTracer.getTag();

webRequestTracer.startTiming();

// perform the request here & do not forget to add the HTTP header

webRequestTracer.setResponseCode(200);
webRequestTracer.stopTiming();

```


## Terminating the OpenKit instance

When an OpenKit instance is no longer needed (e.g. the application using OpenKit is shut down), the previously
obtained instance can be cleared by invoking the `shutdown` method.  
Calling the `shutdown` method blocks the calling thread while the OpenKit flushes data which has not been
transmitted yet to the backend (Dynatrace SaaS/Dynatrace Managed/AppMon).  
Details are explained in [internals.md](#internals.md)