/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.protocol;

/**
 * Event types used in the beacon protocol.
 */
public enum EventType {
	ACTION_MANUAL,			// Action
	VALUE_STRING,			// captured string
	VALUE_INT,				// captured int
	VALUE_DOUBLE,			// captured double
	NAMED_EVENT,			// named event
	SESSION_END,			// session end
	WEBREQUEST,				// tagged web request
	ERROR_CODE;				// error

	public short protocolValue() {
		switch (this) {
			case ACTION_MANUAL:
				return 1;
			case VALUE_STRING:
				return 11;
			case VALUE_INT:
				return 12;
			case VALUE_DOUBLE:
				return 13;
			case NAMED_EVENT:
				return 10;
			case SESSION_END:
				return 19;
			case WEBREQUEST:
				return 30;
			case ERROR_CODE:
				return 40;
			default:
				return -1;
		}
	}

}
