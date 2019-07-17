# OpenKit Java Changelog

## [Unreleased](https://github.com/Dynatrace/openkit-java/compare/v1.3.3...release/1.3)

# 1.3.3 [Release date: 2019-07-17]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v1.3.3)

### Changed
- Fix wrong value for Content-Length HTTP header that was added when sending beacon data.

# 1.3.2 [Release date: 2019-02-19]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v1.3.2)

### Changed
- Fixed problem with infinite time sync requests  
  This problem occurred mainly in AppMon settings.

# 1.3.1 [Release date: 2018-12-19]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v1.3.1)

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
