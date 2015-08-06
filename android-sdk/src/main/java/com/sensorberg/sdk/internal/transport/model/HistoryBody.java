package com.sensorberg.sdk.internal.transport.model;

import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.model.realm.RealmAction;
import com.sensorberg.sdk.model.realm.RealmScan;

import java.util.Date;
import java.util.List;

//serialized by gson
@SuppressWarnings({"unused", "WeakerAccess"})
public class HistoryBody {

    public final List<RealmScan> events;
    public final List<RealmAction> actions;
    public final Date deviceTimestamp;

    public HistoryBody(List<RealmScan> scans, List<RealmAction> actions, Clock clock) {
        this.events = scans;
        this.deviceTimestamp = new Date(clock.now());
        this.actions = actions;
    }
}
