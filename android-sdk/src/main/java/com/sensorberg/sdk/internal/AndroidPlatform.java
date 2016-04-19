package com.sensorberg.sdk.internal;

import com.sensorberg.SensorbergApplication;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.presenter.LocalBroadcastManager;
import com.sensorberg.sdk.presenter.ManifestParser;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class AndroidPlatform implements Platform {

    @Inject
    SharedPreferences settingsPreferences;

    @Inject
    @Named("realClock")
    Clock clock;

    @Inject
    PermissionChecker permissionChecker;

    @Inject
    ServiceScheduler mServiceScheduler;

    private final Context context;

    @Inject
    @Named("androidBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    private static boolean actionBroadcastReceiversRegistered;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AndroidPlatform(Context context) {
        this.context = context;
        SensorbergApplication.getComponent().inject(this);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean isSyncEnabled() {
        if (permissionChecker.hasReadSyncSettingsPermissions()) {
            return ContentResolver.getMasterSyncAutomatically();
        } else {
            return true;
        }
    }

    @Override
    public boolean registerBroadcastReceiver() {
        if (!actionBroadcastReceiversRegistered) {
            List<BroadcastReceiver> broadcastReceiver = getBroadcastReceiver();
            if (broadcastReceiver.isEmpty()) {
                return false;
            }
            registerBroadcastReceiver(broadcastReceiver);
            actionBroadcastReceiversRegistered = true;
        }
        return true;
    }

    @Override
    public String getHostApplicationId() {
        return context.getPackageName();
    }

    @Override
    public List<BroadcastReceiver> getBroadcastReceiver() {
        return ManifestParser.findBroadcastReceiver(context);
    }

    @Override
    public void registerBroadcastReceiver(List<BroadcastReceiver> broadcastReceiver) {
        for (BroadcastReceiver receiver : broadcastReceiver) {
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(ManifestParser.actionString));
        }
    }

    @Override
    public boolean isBluetoothLowEnergySupported() {
        return bluetoothPlatform.isBluetoothLowEnergySupported();
    }

}
