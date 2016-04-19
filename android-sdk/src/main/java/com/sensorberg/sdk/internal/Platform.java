package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.internal.interfaces.Transport;

import android.content.BroadcastReceiver;

import java.util.List;

public interface Platform {

    Transport getTransport();

    boolean isSyncEnabled();

    String getHostApplicationId();

    List<BroadcastReceiver> getBroadcastReceiver();

    boolean registerBroadcastReceiver();

    void registerBroadcastReceiver(List<BroadcastReceiver> broadcastReceiver);

    interface ForegroundStateListener {

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
     * Returns a flag indicating whether Bluetooth is supported.
     *
     * @return a flag indicating whether Bluetooth is supported
     */
    boolean isBluetoothLowEnergySupported(); //TODO remove after integrating Bootstrapper

}
