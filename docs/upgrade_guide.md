# Upgrade guide for OpenKit Java

## OpenKit Java 2.2 to 3.0
Appmon has been removed from OpenKit Java. If you don't want to replace your AppMon related code stay on the latest 2.2.x release.

### Removed API
* `AbstractOpenKitBuilder` has been removed as it was not needed anymore due to AppMon removal. All functionalities have been consolidated into the `DynatraceOpenKitBuilder`.
* `DynatraceOpenKitBuilder(String endpointURL, String applicationID, String deviceID)`
  Use `DynatraceOpenKitBuilder(String endpointURL, String applicationID, long deviceID)` instead.
* `DynatraceOpenKitBuilder#withApplicationName(String applicationName)`
  The application name is configured in Dynatrace Web UI.
* `DynatraceOpenKitBuilder#getApplicationName()`
  The application name is configured in Dynatrace Web UI.
* `OpenKitConfiguration#getApplicationName()`
  The application name is configured in Dynatrace Web UI.
* ` Action#reportError(String errorName, int errorCode, String reason)`
  Use ` Action#reportError(String errorName, int errorCode)` without `String reason` argument, as
  `reason` is unhandled in Dynatrace.
* `WebRequestTracer#setResponseCode(int responseCode)` and `WebRequestTracer#stop()`
  Use `WebRequestTracer#stop(int responseCode)` instead as replacement.
* `DynatraceOpenKitBuilder#enableVerbose()`
  Use `DynatraceOpenKitBuilder#withLogLevel(LogLevel.DEBUG)` instead.

## OpenKit Java 2.1 to 2.2
There are no breaking API changes and upgrading is straightforward, by [updating][update] the library
to the latest 2.2 release.

## OpenKit Java 2.0 to 2.1
There are no breaking API changes and upgrading is straightforward, by [updating][update] the library
to the latest 2.1 release.

### Deprecated API
* ` Action#reportError(String errorName, int errorCode, String reason)`
  Use ` Action#reportError(String errorName, int errorCode)` without `String reason` argument, as
  `reason` is unhandled in Dynatrace.

## OpenKit Java 1.4 to 2.0
There are no breaking API changes and upgrading is straightforward, by [updating][update] the library
to the latest 2.0 release.

### Deprecated API
* `WebRequestTracer#setResponseCode(int responseCode)` and `WebRequestTracer#stop()`  
  Use `WebRequestTracer#stop(int responseCode)` instead as replacement.
* `DynatraceOpenKitBuilder#withApplicationName(String applicationName)`  
  The application name is configured in Dynatrace Web UI.
* `AbstractOpenKitBuilder#enableVerbose()`
  Use `AbstractOpenKitBuilder#withLogLevel(LogLevel.DEBUG)` instead.

## OpenKit Java 1.3 and below to 1.4
There are no breaking API changes and upgrading is straightforward, by [updating][update] the library
to the latest 1.4 release.

### Deprecated API
* `DynatraceOpenKitBuilder(String endpointURL, String applicationID, String deviceID)`  
   Use `DynatraceOpenKitBuilder(String endpointURL, String applicationID, long deviceID)` instead.

[update]: ./installing.md#Updating-OpenKit-Java