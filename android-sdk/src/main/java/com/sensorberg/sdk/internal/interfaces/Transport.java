package com.sensorberg.sdk.internal.interfaces;

import com.sensorberg.sdk.internal.transport.TransportHistoryCallback;
import com.sensorberg.sdk.internal.transport.TransportSettingsCallback;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;

import java.util.List;

public interface Transport {

    String ADVERTISING_IDENTIFIER = "X-aid";

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

    void loadSettings(TransportSettingsCallback transportSettingsCallback);

    void publishHistory(List<SugarScan> scans, List<SugarAction> actions, TransportHistoryCallback callback);

    void updateBeaconLayout();

    void setBeaconHistoryUploadIntervalListener(BeaconHistoryUploadIntervalListener listener);
}
