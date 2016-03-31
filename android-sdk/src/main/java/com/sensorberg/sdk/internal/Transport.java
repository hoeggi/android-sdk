package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.internal.transport.HistoryCallback;
import com.sensorberg.sdk.internal.transport.SettingsCallback;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;

import java.util.List;

public interface Transport {

    interface ProximityUUIDUpdateHandler{
        ProximityUUIDUpdateHandler NONE = new ProximityUUIDUpdateHandler() {
            @Override
            public void proximityUUIDListUpdated(List<String> proximityUUIDs) {

            }
        };

        void proximityUUIDListUpdated(List<String> proximityUUIDs);
    }

    interface BeaconReportHandler {
        BeaconReportHandler NONE = new BeaconReportHandler() {
            @Override
            public void reportImmediately() {

            }
        };

        void reportImmediately();
    }
    void setBeaconReportHandler(BeaconReportHandler beaconReportHandler);

    void setProximityUUIDUpdateHandler(ProximityUUIDUpdateHandler proximityUUIDUpdateHandler);

    void getBeacon(ResolutionConfiguration resolutionConfiguration, BeaconResponseHandler beaconResponseHandler);

    void setApiToken(String apiToken);

    void getSettings(SettingsCallback settingsCallback);

    void publishHistory(List<SugarScan> scans, List<SugarAction> actions, HistoryCallback callback);

    void updateBeaconLayout();
}
