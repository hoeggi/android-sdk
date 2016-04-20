package com.sensorberg.sdk.internal.transport;

import com.android.sensorbergVolley.VolleyError;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;

import java.util.List;

public interface HistoryCallback {
    void onFailure(VolleyError throwable);

    void onInstantActions(List<BeaconEvent> instantActions);

    void onSuccess(List<SugarScan> scans, List<SugarAction> actions);
}
