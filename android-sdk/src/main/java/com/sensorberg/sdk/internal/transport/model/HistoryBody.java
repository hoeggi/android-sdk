package com.sensorberg.sdk.internal.transport.model;

import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;

import java.util.Date;
import java.util.List;

//serialized by gson
@SuppressWarnings({"unused", "WeakerAccess"})
public class HistoryBody {

    public final List<SugarScan> events;
    public final List<SugarAction> actions;
    public final Date deviceTimestamp;

    public HistoryBody(List<SugarScan> scans, List<SugarAction> actions, Clock clock) {
        this.events = scans;
        this.deviceTimestamp = new Date(clock.now());
        this.actions = actions;
    }
}
