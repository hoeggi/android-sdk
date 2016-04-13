package com.sensorberg.sdk.internal.interfaces;

import com.sensorberg.sdk.settings.Settings;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.os.Build;

import java.util.List;

public interface Platform {

    String getUserAgentString();

    String getDeviceInstallationIdentifier();

    String getAdvertiserIdentifier();

    Transport getTransport();

    boolean useSyncClient();

    boolean isSyncEnabled();

    boolean hasMinimumAndroidRequirements();

    void setSettings(Settings settings);

    String getHostApplicationId();

    List<BroadcastReceiver> getBroadcastReceiver();

    boolean registerBroadcastReceiver();

    void registerBroadcastReceiver(List<BroadcastReceiver> broadcastReceiver);

    void addDeviceInstallationIdentifierChangeListener(DeviceInstallationIdentifierChangeListener listener);

    void addAdvertiserIdentifierChangeListener(AdvertiserIdentifierChangeListener listener);

    interface ForegroundStateListener{

        ForegroundStateListener NONE = new ForegroundStateListener() {
            @Override
            public void hostApplicationInBackground() {

            }

            @Override
            public void hostApplicationInForeground() {

            }
        };

        void hostApplicationInBackground();

        void hostApplicationInForeground();
    }

    /**
     * Returns a flag indicating whether Bluetooth is enabled.
     *
     * @return a flag indicating whether Bluetooth is enabled
     */
    boolean isBluetoothLowEnergyDeviceTurnedOn();

    /**
     * Returns a flag indicating whether Bluetooth is supported.
     *
     * @return a flag indicating whether Bluetooth is supported
     */
    boolean isBluetoothLowEnergySupported();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void startLeScan(BluetoothAdapter.LeScanCallback scanCallback);

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void stopLeScan();

    boolean isLeScanRunning();

    boolean isBluetoothEnabled();

    /**
     * Interface for device installation identifier.
     */
    interface DeviceInstallationIdentifierChangeListener {
        void deviceInstallationIdentifierChanged(String deviceInstallationIdentifier);
    }

    /**
     * Interface for advertising identifier.
     */
    interface AdvertiserIdentifierChangeListener {
        void advertiserIdentifierChanged(String advertiserIdentifier);
    }

}
