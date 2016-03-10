package com.sensorberg.sdk.internal;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import com.android.sensorbergVolley.RequestQueue;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.settings.Settings;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface Platform {

    String getUserAgentString();

    String getDeviceInstallationIdentifier();

    String getGoogleAdvertisingIdentifier();

    Transport getTransport();

    File getFile(String fileName);

    boolean useSyncClient();

    NotificationManager getNotificationManager();

    Context getContext();

    boolean isSyncEnabled();

    boolean hasMinimumAndroidRequirements();

    SharedPreferences getSettingsSharedPrefs();

    void scheduleRepeating(int MSG_index, long value, TimeUnit timeUnit);

    void postToServiceDelayed(long delay, int type, Parcelable what, boolean surviveReboot);

    void postToServiceDelayed(long delay, int type, Parcelable what, boolean surviveReboot, int index);

    void cancel(int message);

    void scheduleIntent(long key, long delayInMillis, Bundle content);

    void setSettings(Settings settings);

    void unscheduleIntent(int index);

    void write(Serializable serializableObject, String fileName);

    void removeFile(String fileName);

    void cancelAllScheduledTimer();

    String getHostApplicationId();

    void cancelServiceMessage(int index);

    RequestQueue getVolleyQueue();

    List<BroadcastReceiver> getBroadcastReceiver();

    boolean registerBroadcastReceiver();

    void registerBroadcastReceiver(List<BroadcastReceiver> broadcastReceiver);

    void setShouldUseHttpCache(boolean shouldUseHttpCache);

    void postDeliverAtOrUpdate(Date deliverAt, BeaconEvent beaconEvent);

    void clearAllPendingIntents();

    void restorePendingIntents();

    void removeStoredPendingIntent(int index);

    void addDeviceInstallationIdentifierChangeListener(DeviceInstallationIdentifierChangeListener listener);

    void addGoogleAdvertisingIdentifierChangeListener(GoogleAdvertisingIdentifierChangeListener listener);

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

    Clock getClock();

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

    RunLoop getScannerRunLoop(RunLoop.MessageHandlerCallback callback);

    RunLoop getResolverRunLoop(RunLoop.MessageHandlerCallback callback);

    RunLoop getBeaconPublisherRunLoop(RunLoop.MessageHandlerCallback callback);

    interface DeviceInstallationIdentifierChangeListener {
        void deviceInstallationIdentifierChanged(String deviceInstallationIdentifier);
    }

    /**
     * Interface for advertising identifier.
     */
    interface GoogleAdvertisingIdentifierChangeListener {
        void googleAdvertisingIdentifierChanged(String googleAdvertisingIdentifier);
    }

}
