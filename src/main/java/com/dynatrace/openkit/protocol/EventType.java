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
    ACTION,                    // Action
    VALUE_STRING,            // captured string
    VALUE_INT,                // captured int
    VALUE_DOUBLE,            // captured double
    NAMED_EVENT,            // named event
    SESSION_END,            // session end
    WEBREQUEST,                // tagged web request
    ERROR,                    // error
    CRASH,                    // crash
    IDENTIFY_USER;            // identify user

    public short protocolValue() {
        switch (this) {
            case ACTION:
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
            case ERROR:
                return 40;
            case CRASH:
                return 50;
            case IDENTIFY_USER:
                return 60;
            default:
                return -1;
        }
    }

}
