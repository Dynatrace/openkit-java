/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.core.objects.SessionProxyImpl;
import com.dynatrace.openkit.providers.TimingProvider;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A context keeping track of sessions which could not be finished after session splitting by events and which will
 * be closed after a grace period.
 */
public class SessionWatchdogContext {

    // the default sleep time if no session is to be closed / split.
    static final long DEFAULT_SLEEP_TIME_IN_MILLIS = TimeUnit.SECONDS.toMillis(5);

    //  Indicator whether shutdown was requested or not.
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    // timing provider for suspending the current thread for a certain amount of time
    private final TimingProvider timingProvider;
    // holds all sessions which are to be closed after a certain grace period
    private final LinkedBlockingQueue<SessionImpl> sessionsToClose = new LinkedBlockingQueue<>();
    // holds all session proxies which are to be split after expiration of either session duration or idle timeout.
    private final LinkedBlockingQueue<SessionProxyImpl> sessionsToSplitByTimeout = new LinkedBlockingQueue<>();

    public SessionWatchdogContext(TimingProvider timingProvider) {
        this.timingProvider = timingProvider;
    }

    public void execute() {
        long durationToNextCloseInMillis = closeExpiredSessions();
        long durationToNextSplitInMillis = splitTimedOutSessions();

        try {
            long sleepTime = Math.min(durationToNextCloseInMillis, durationToNextSplitInMillis);
            timingProvider.sleep(sleepTime);
        } catch (InterruptedException e) {
            requestShutdown();
            Thread.currentThread().interrupt();
        }
    }

    private long splitTimedOutSessions() {
        long sleepTimeInMillis = DEFAULT_SLEEP_TIME_IN_MILLIS;
        Iterator<SessionProxyImpl> sessionProxyIterator = sessionsToSplitByTimeout.iterator();
        while (sessionProxyIterator.hasNext()) {
            SessionProxyImpl sessionProxy = sessionProxyIterator.next();

            long nextSessionSplitTimeInMillis = sessionProxy.splitSessionByTime();
            if (nextSessionSplitTimeInMillis < 0) {
                sessionProxyIterator.remove();
                continue;
            }

            long nowInMillis = timingProvider.provideTimestampInMilliseconds();
            long durationToNextSplit = nextSessionSplitTimeInMillis - nowInMillis;
            if (durationToNextSplit < 0) {
                continue;
            }

            sleepTimeInMillis = Math.min(sleepTimeInMillis, durationToNextSplit);
        }

        return sleepTimeInMillis;
    }

    private long closeExpiredSessions() {
        long sleepTimeInMillis = DEFAULT_SLEEP_TIME_IN_MILLIS;
        List<SessionImpl> sessionsToEnd = new LinkedList<>();
        Iterator<SessionImpl> sessionIterator = sessionsToClose.iterator();
        while (sessionIterator.hasNext()) {
            SessionImpl session = sessionIterator.next();
            long nowInMillis = timingProvider.provideTimestampInMilliseconds();
            long gracePeriodEndTimeInMillis = session.getSplitByEventsGracePeriodEndTimeInMillis();
            boolean isGracePeriodExpired = gracePeriodEndTimeInMillis <= nowInMillis;
            if (isGracePeriodExpired) {
                sessionIterator.remove();
                sessionsToEnd.add(session);
                continue;
            }

            long sleepTimeToGracePeriodEndInMillis = gracePeriodEndTimeInMillis - nowInMillis;
            sleepTimeInMillis = Math.min(sleepTimeInMillis, sleepTimeToGracePeriodEndInMillis);
        }

        for (SessionImpl session : sessionsToEnd) {
            session.end(false);
        }

        return sleepTimeInMillis;
    }

    /**
     * Requests shutdown
     */
    public void requestShutdown() {
        shutdown.set(true);
    }

    /**
     * indicates whether shutdown was requested before or not.
     */
    public boolean isShutdownRequested() {
        return shutdown.get();
    }

    /**
     * Tries to close the given session. If closing the session is currently not possible (e.g. due to still ongoing
     * child actions / web request tracers) it will be enqueued to be forcefully closed after the given grade period
     * is expired.
     *
     * @param session the session to be closed or enqueued
     * @param closeGracePeriodInMillis the grace period after which the session is closed for good.
     */
    public void closeOrEnqueueForClosing(SessionImpl session, long closeGracePeriodInMillis) {
        if (session.tryEnd()) {
            return;
        }
        long closeTime = timingProvider.provideTimestampInMilliseconds() + closeGracePeriodInMillis;
        session.setSplitByEventsGracePeriodEndTimeInMillis(closeTime);
        sessionsToClose.add(session);
    }

    /**
     * Removes the given session from auto-closing after a certain grace period.
     * @param session the session to be removed.
     */
    public void dequeueFromClosing(SessionImpl session) {
        sessionsToClose.remove(session);
    }

    LinkedBlockingQueue<SessionImpl> getSessionsToClose() {
        return sessionsToClose;
    }

    /**
     * Adds the given session proxy so that it will be automatically split the underlying session when the idle timeout
     * or the max session time is reached.
     *
     * @param sessionProxy the session proxy to be added.
     */
    public void addToSplitByTimeout(SessionProxyImpl sessionProxy) {
        if (sessionProxy.isFinished()) {
            return;
        }
        sessionsToSplitByTimeout.add(sessionProxy);
    }

    /**
     * Removes the given session proxy from automatically splitting it after idle or session max time expired.
     *
     * @param sessionProxy the session proxy to be removed.
     */
    public void removeFromSplitByTimeout(SessionProxyImpl sessionProxy) {
        sessionsToSplitByTimeout.remove(sessionProxy);
    }

    LinkedBlockingQueue<SessionProxyImpl> getSessionsToSplitByTimeout() {
        return sessionsToSplitByTimeout;
    }
}
