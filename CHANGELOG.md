# OpenKit Java Changelog

## [Unreleased](https://github.com/Dynatrace/openkit-dotnet/compare/v1.1.1...HEAD)

### Changed
- Fix wrong SimpleDatePattern, which does not work with Java 6

## 1.1.1 [Release date: 2018-09-03]
[GitHub Releases](https://github.com/Dynatrace/openkit-java/releases/tag/v1.1.1)

### Changed
- Fix wrong Session start time

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
