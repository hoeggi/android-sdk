package com.sensorberg.sdk.internal.transport.interfaces;

import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;

import java.util.List;

public interface TransportHistoryCallback {
    void onFailure(Exception throwable);

    void onInstantActions(List<BeaconEvent> instantActions);

    void onSuccess(List<SugarScan> scans, List<SugarAction> actions);
}
