package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.interfaces.BeaconResponseHandler;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.internal.transport.HistoryCallback;
import com.sensorberg.sdk.internal.transport.TransportSettingsCallback;
import com.sensorberg.sdk.model.realm.RealmAction;
import com.sensorberg.sdk.model.realm.RealmScan;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;

import java.util.List;

public class DumbSucessTransport implements Transport {

    @Override
    public void updateBeaconLayout() {

    }

    @Override
    public void setBeaconReportHandler(BeaconReportHandler beaconReportHandler) {

    }

    @Override
    public void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler) {

    }

    @Override
    public void getBeacon(ResolutionConfiguration resolutionConfiguration, BeaconResponseHandler beaconResponseHandler) {
        beaconResponseHandler.onFailure(new IllegalArgumentException("this transport is dumb"));
    }

    @Override
    public void setApiToken(String apiToken) {

    }

    @Override
    public void setSettingsCallback(TransportSettingsCallback transportSettingsCallback) {

    }

    @Override
    public void publishHistory(List<RealmScan> scans, List<RealmAction> actions, HistoryCallback callback) {
        callback.onSuccess(scans,actions);
    }
}
