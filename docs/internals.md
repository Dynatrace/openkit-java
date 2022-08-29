# OpenKit - Internals

## Data Sending (Beacon Sending)

All data sending, including synchronization with the backend (Dynatrace SaaS/Dynatrace Managed)
happens asynchronously by starting an own thread when OpenKit is initialized.  

Beacon sending in OpenKit is implemented using a state pattern. The following 
diagram illustrates the states.

![diagram](./pics/OpenKit-state_diagram.svg)

### Initialize

The Init state (class `BeaconSendingInitState`) sends the initial status request to the server.
The request is retried several times (by default 5 retries, in total 6 requests) with increasing
delays between consecutive retries.  
If the server returned a successful status response a state transition to either CaptureOn or CaptureOff
is performed, depending on whether capturing is enabled or disabled in the initial status response.
If the status request fails OpenKit stays in the Initialize state and sleeps some time
until the next status request is sent. 
 
If `OpenKit.shutdown()` is called while OpenKit is in the Init state, 
a transition to the Terminal state is performed.

### CaptureOff

In the CaptureOff state (class `BeaconSendingCaptureOffState`) OpenKit checks when the last
status request was sent to the server and sleeps some time before performing the next status
request.  
A transition to CaptureOn state is performed if capturing was re-enabled by the server's status response.
If capturing is disabled no transition is performed and the state machine stays in CaptureOff state.  

If OpenKit is shut down during CaptureOff state a transition to FlushSessions is performed.

### CaptureOn

In the CaptureOn state (class `BeaconSendingCaptureOnState`) OpenKit checks in regular intervals
(the default value is 1 second) if it should send open sessions. The interval for sending
open sessions is configured in the status response.  
Furthermore all previously finished sessions are also sent to the server.  

Data sending is retried three times to avoid data loss with increasing delays between consecutive
retries.

If OpenKit is shut down during CaptureOn state a transition to FlushSessions is performed.

### FlushSessions

The FlushSessions state (class `BeaconSendingFlushSessionsState`) is used to send all
data which has not been transferred so far to the server.

### Terminal

The Terminal state (class `BeaconSendingTerminalState`) is the last state in OpenKit's internal 
state machine. After this state is reached the background thread responsible for sending data 
is terminated gracefully.

## Data Capturing (BeaconCache)

To be able to capture events while OpenKit is still initializing, all reported events are stored
in a cache, the so called BeaconCache.

An illustration how this cache looks like can be found below, followed by a detailed explanation.

![diagram](./pics/OpenKit-BeaconCache.svg)

### Basic BeaconCache Layout

The BeaconCache is a 2-dimensional cache, storing data for each Session, internally also referred to
as Beacon. Each OpenKit instance has such a cache, so if your application starts multiple OpenKit instances
in parallel, keep in mind that each OpenKit instance consumes cache memory.  

The example image shows three Sessions started from one OpenKit instance and for each Session a different number of Event Data
and Action Data.

### BeaconCache Records

A record is a single captured event, like an Action, a Web Request or anything else captured with
OpenKit. A record is already serialized data which can be sent to the backend system.

### BeaconCache Eviction

By default the BeaconCache has two eviction strategies, which are triggered whenever new data
is inserted. Triggering such a strategy does not necessarily mean that records are evicted from the cache, but rather
the strategy evaluates whether it makes sense to run or not.

The eviction strategies run in a separate background thread, which is started when OpenKit is started and
shut down when OpenKit is terminated.

#### Time Based Eviction

Since Dynatrace backend services does not process too old data, it make sense to not send such data to the
backend system.
By default records that are older than 45 minutes are evicted, but when initializing an
OpenKit instance via the builder the value can be set by calling `withBeaconCacheMaxRecordAge` with an argument specifying the
maximum record age in milliseconds.

It is possible to disable this strategy by setting the argument to `withBeaconCacheMaxRecordAge` to a value less than
or equal to 0.

#### Size based Eviction

The second eviction strategy is used to limit the memory consumption of OpenKit.
This strategy evicts beacons if the memory usage of the BeaconCache exceeds a configured upper bound and stops
as soon as the memory consumed by the cache reaches a configured lower bound.
By default, the upper boundary is set to 100 megabytes and the lower boundary is 80 megabytes.
The defaults can be changed when initializing the OpenKit instance via the builder by calling `withBeaconCacheLowerMemoryBoundary`
and `withBeaconCacheUpperMemoryBoundary`.

When the upper boundary is set to a value less than or equal to the lower boundary, this strategy is disabled.

### BeaconCache and Threading

The cache itself is implemented in a thread safe manner. It is limiting the time when shared resources are locked to a 
bare minimum. Furthermore the cache makes also use of Read-Write-Locks to ensure maximum parallelism when different
Sessions (Beacons) are accessed.

## Session splitting

Session splitting describes the process of closing / trying to close the current active session and start a new session,
when a certain split condition is met. From an OpenKit user's perspective session splitting is transparent, in the sense
that no explicit action needs to be taken. Internally OpenKit is returning a handle to a session proxy when a new session
is created. This session proxy is keeping track of the real current active session and is forwarding all top level event
calls (`identifyUser`, `enterAction`, `traceWebRequest` and `reportCrash`). When a session split condition is met the 
current active session will be replaced with a newly created session within the session proxy. Subsequent top level
event calls are then forwarded to this new session. The split condition is either based on top level action (`RootAction`)
count or on the expiration of a certain timeout.

### Session splitting by event count

A session may be split after a maximum number of top level actions. The concrete number is sent by the Dynatrace backend
as response to any query (status request, new session request, beacon request) done by OpenKit. In case the maximum 
number of top level events is not sent by Dynatrace backend, session splitting by events will not be done. 

To decide when a session needs to be split, all top level action invocations (calling `enterAction` on the session) are 
counted for the current session. When the counter exceeds the maximum number of top level actions, the session is split 
and the counter is restarted from zero. The old session is ended/closed immediately, if there are no more open Actions 
or Web Requests. Otherwise, it will be kept open for a certain grace period. After expiration of the grace period the 
session will be forcefully closed if it was not already closed manually in the meantime. 

### Session splitting by timeout

Since sessions are automatically closed on the Dynatrace backend side after a certain idle timeout or after the 
expiration of the maximum session timeout, it makes no sense for OpenKit to still keep recording events to such sessions.
Instead OpenKit will close the current session and create a new one in case the idle or the maximum session timeout 
expired, by keeping track of the times when the session was started/created and when the last top level event 
(`identifyUser`, `enterAction`, `traceWebRequest` or `reportCrash`) was invoked. The idle and maximum session timeout 
are defined by the Dynatrace backend and might be sent as response to any OpenKit query (status request, new session 
request, beacon request). If the timeout value is not sent by the Dynatrace backend, the concrete session split strategy
will not be done. E.g. if no idle timeout is sent by the backend, sessions will not be split after idling.

### Session watchdog thread

The session watchdog is a separate thread which's task is to split sessions after expiration of the idle or maximum 
session timeout. Additionally it keeps track of old sessions that could not be closed after session splitting by events
(due to open Actions or Web Requests) and which are to be closed after a certain grace period. 


### Identify Users on split sessions

OpenKit re-applies the last user identification, which was reported with `identifyUser` on every session, split after
the API call. This behavior is only implemented for client-side session splitting and will implicitly send the
same data as an `identifyUser` API does, therefore it is shown as first event in the split session. 
To stop re-tagging sessions `identifyUser` can be called with `null` or an empty string.
