# Upgrade guide for OpenKit Java

## OpenKit Java 1.4 to 2.0
There are no breaking API changes and upgrading is straightforward, by [updating][update] the library
to the latest 2.0 release.

### Deprecated API
* `WebRequestTracer#setResponseCode(int responseCode)` and `WebRequestTracer#stop()`  
  Use `WebRequestTracer#stop(int responseCode)` instead as replacement.
* `DynatraceOpenKitBuilder#withApplicationName(String applicationName)`  
  The application name is configured in Dynatrace Web UI.

## OpenKit Java 1.3 and below to 1.4
There are no breaking API changes and upgrading is straightforward, by [updating][update] the library
to the latest 1.4 release.

### Deprecated API
* `DynatraceOpenKitBuilder(String endpointURL, String applicationID, String deviceID)`  
   Use `DynatraceOpenKitBuilder(String endpointURL, String applicationID, long deviceID)` instead.
* `AppMonOpenKitBuilder(String endpointURL, String applicationName, String deviceID)`  
   Use `AppMonOpenKitBuilder(String endpointURL, String applicationName, long deviceID)` instead.

[update]: ./installing.md#Updating-OpenKit-Java