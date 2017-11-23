/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import java.net.URLConnection;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.WebRequestTag;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.TimeProvider;

/**
 * Actual implementation of the {@link Action} interface.
 */
public class ActionImpl implements Action {

	// Action ID, name and parent ID (default: null)
	private final int id;
	private final String name;
	private ActionImpl parentAction = null;

	// start/end time & sequence number
	private final long startTime;
	private long endTime = -1;
	private final int startSequenceNo;
	private int endSequenceNo = -1;

	// Beacon reference
	private Beacon beacon;

	private SynchronizedQueue<Action> thisLevelActions = null;

	// *** constructors ***

	ActionImpl(Beacon beacon, String name, SynchronizedQueue<Action> parentActions) {
		this(beacon, name, null, parentActions);
	}

	ActionImpl(Beacon beacon, String name, ActionImpl parentAction, SynchronizedQueue<Action> thisLevelActions) {
		this.beacon = beacon;
		this.parentAction = parentAction;

		this.startTime = TimeProvider.getTimestamp();
		this.startSequenceNo = beacon.createSequenceNumber();
		this.id = beacon.createID();
		this.name = name;

		this.thisLevelActions = thisLevelActions;
		this.thisLevelActions.put(this);
	}

	// *** Action interface methods ***

	@Override
	public Action reportEvent(String eventName) {
		beacon.reportEvent(this, eventName);
		return this;
	}

	@Override
	public Action reportValue(String valueName, int value) {
		beacon.reportValue(this, valueName, value);
		return this;
	}

	@Override
	public Action reportValue(String valueName, double value) {
		beacon.reportValue(this, valueName, value);
		return this;
	}

	@Override
	public Action reportValue(String valueName, String value) {
		beacon.reportValue(this, valueName, value);
		return this;
	}

	@Override
	public Action reportError(String errorName, int errorCode, String reason) {
		beacon.reportError(this, errorName, errorCode, reason);
		return this;
	}

	@Override
	public WebRequestTag tagWebRequest(URLConnection connection) {
		return new WebRequestTagURLConnection(beacon, this, connection);
	}

	@Override
	public WebRequestTag tagWebRequest(String url) {
		return new WebRequestTagStringURL(beacon, this, url);
	}

	@Override
	public Action leaveAction() {
		// check if leaveAction() was already called before by looking at endTime
		if (endTime != -1) {
			return parentAction;
		}

		return doLeaveAction();
	}

	protected Action doLeaveAction() {
		// set end time and end sequence number
		endTime = TimeProvider.getTimestamp();
		endSequenceNo = beacon.createSequenceNumber();

		// add Action to Beacon
		beacon.addAction(this);

		// remove Action from the Actions on this level
		thisLevelActions.remove(this);

		return parentAction;			// can be null if there's no parent Action!
	}

	// *** getter methods ***

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getParentID() {
		return parentAction == null ? 0 : parentAction.getID();
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public int getStartSequenceNo() {
		return startSequenceNo;
	}

	public int getEndSequenceNo() {
		return endSequenceNo;
	}

}
