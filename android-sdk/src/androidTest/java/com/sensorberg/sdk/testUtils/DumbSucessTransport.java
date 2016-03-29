package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.BeaconResponseHandler;
import com.sensorberg.sdk.internal.Transport;
import com.sensorberg.sdk.internal.transport.HistoryCallback;
import com.sensorberg.sdk.internal.transport.SettingsCallback;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
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
    public void getSettings(SettingsCallback settingsCallback) {

    }

    @Override
    public void publishHistory(List<SugarScan> scans, List<SugarAction> actions, HistoryCallback callback) {
        callback.onSuccess(scans,actions);
    }
}
