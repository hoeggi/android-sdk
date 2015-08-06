package com.sensorberg.sdk.scanner;

import java.io.Serializable;

public class EventEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    long lastBeaconTime;
    private final int  eventMask;

    EventEntry(EventEntry other) {
        this.lastBeaconTime = other.lastBeaconTime;
        this.eventMask = other.eventMask;
        //we do not copy the restoredTimestamp since it is irrelevant...
    }

    EventEntry(long lastBeaconTime, int eventMask) {
        this.lastBeaconTime = lastBeaconTime;
        this.eventMask = eventMask;
        //we do not copy the restoredTimestamp since it is irrelevant...
    }
}
