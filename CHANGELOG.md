# OpenKit Java Changelog

## [Unreleased](https://github.com/Dynatrace/openkit-java/compare/v2.1.0...HEAD)

### Added
- Support cost control configured in the Dynatrace UI.
- It is possible to get the duration from an `Action`.
- An `Action` can be cancelled.  
  Cancelling an Action is similar to leaving it, without reporting it.

### Changed
- Provide a more reliable way to determine monotonic timestamps.
- Fix potential endless loop in beacon sending, when lots of data
  is generated in a short period of time.

## 2.1.0 [Release date: 2020-11-16]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v2.1.0)

### Added
- Overloaded `Action.reportValue(String, long`) method for reporting 64-bit integer values.
- Overloaded `Session.reportCrash(Throwable)` as convenience method for reporting a `Throwable` as crash.
- Overloaded `Action.reportError(String, int)` method for reporting an integer error code without description.  
  The old `Action.reportError(String, int, String)` has been deprecated in favor of the new one.
- Overloaded `Action.reportError(String, Throwable)` for reporting caught exceptions as error.
- Overloaded `Action.reportError(String, String, String, String)` for reporting generic errors to
  Dynatrace.

### Changed
- Fix issue with sessions being closed after splitting.
  This happened because OpenKit was sending an end session event right after splitting.
  New behavior is to only send the end session event if explicitly requested via
  the `Session.end()` method and only for the active session.
- `identifyUser` can be called with `null` or an empty string.  
  This simulates a log off event and no longer re-applies the user tag on split sessions.
- Improve handling of client IP in combination with server-side detection.
- Fix potential NPE occurring with empty response body.
- Fix multithreading issues in `SessionProxy`, leading to a potential NPE.
- Fix potential memory leak for very short-lived sessions in `SessionProxy`.

## 2.0.0 [Release date: 2020-06-24]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v2.0.0)

### Added
- Technology type support for error and crashes
- Support for session splitting. Sessions are split transparently after either the maximum session duration,
  the idle timeout or the number of top level actions are exceeded. Session splitting is only applicable,
  if it is also supported by Dynatrace. The internal details are described [here](./docs/internals.md#session-splitting).
- Re-apply user tag on split sessions.

### Changed
- Response code is now a parameter of WebRequestTracer's stop method.
  Existing methods for stopping and setting the response code have been deprecated.
- Fix wrong value for Content-Length HTTP header that was added when sending beacon data.
- Fix sending of session number in web request tracer tag to honor the data collection level.
  The session number will only be sent with data collection level 'User Behavior'.
- On OpenKitBuilder creation device ID is parsed from the given string. Non-numeric
  device IDs are hashed to a corresponding numeric value. Internally a numeric
  type is used for the device ID.
- Add OpenKit.createSession overload without IP address parameter.  
  The IP address is determined in this case on the server side.
- Fix taking over HTTP headers (specifically the `retry-after` field) when receiving an HTTP response with
  response codes >= 400.
- Reporting a crash causes a session split, which is transparently handled

### Improved
- Fixed some javadoc/comments in the JSON parser
- Adapt some JSON lexer unit tests to make them consistent with the test's name.

### Improvements
- Reformatted text files to unix style line endings.

## 1.4.0 [Release date: 2018-12-19]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v1.4.0)

### Added
- Java 11 Support

### Changed
- Application ID and Device ID are correctly encoded for special characters
  The encoding is a percent-encoding based on RFC 3986 with additional encoding of underscore characters.
- Set file encoding to UTF-8 in gradle build

## 1.3.0 [Release date: 2018-10-25]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v1.3.0)

### Added
- Device ID can be specified as String in addition to long  
  This allows to send UUIDs or other specifiers
- Standalone web request tagging  
  A WebRequestTracer can also be obtained from a Session

## 1.2.0 [Release date: 2018-09-14]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v1.2.0)

### Added
- Server overload prevention  
  Additional HTTP 429 response code handling

### Changed
- Fix wrong Session start time
- Fix wrong SimpleDatePattern, which does not work with Java 6
- Fix wrong device ID in web requests  
  This has only an impact, if `DataCollectionLevel.PERFORMANCE` was used

### Improved
- OpenKit internal version handling

## 1.1.0 [Release date: 2018-07-20]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v1.1.0)

### Added
- Extend OpenKit objects from java.io.Closeable to add try-with-resources statement compatibility  
  The following objects are affected
  - OpenKit
  - Session
  - RootAction/Action
  - WebRequestTracer
- Build support on Jenkins CI server
- coveralls.io Coverage integration
- Server overload prevention
- GDPR compliance

### Changed
- BeaconSender's thread name is now correct
- Enhanced `null` checks in public interface implementation  
  No exception are thrown, if nulls are passed via public interfaces
- OpenKit internal threads are daemon threads
- Further actions on already left/closed OpenKit objects are no longer possible  
  Calling the methods is still allowed, but nothing is reported to the backend 
- HTTPClient checks response type from server when parsing
- Thread IDs are no longer reported as 64-bit integer, but as 32-bit integer  
  Only positive integers are used, since the sign bit is always 0.
- Enhanced state transition in internal state engine
- WebRequestTracer's start time is initialized in constructor
- Advanced URL validation when tracing web requests  
  The URL must have the form  
  `<scheme>://<any character>[<any character>]*`  
  Where scheme must be as defined in RFC 3986
- InetAddress validation for IPv6 mixed mode addresses
- Major logging improvements (more and better messages)

### Improved
- Unit tests in protocol package
- Enhanced BeaconCache documentation
- Various typos fixed
- Testing improvements (method names, wrong assertions)

## 1.0.1 [Release date: 2018-01-29]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v1.0.1)
### Initial public release
