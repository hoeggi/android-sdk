package com.sensorberg.sdk.internal;

import com.sensorberg.SensorbergApplicationBootstrapper;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.presenter.LocalBroadcastManager;
import com.sensorberg.sdk.presenter.ManifestParser;
import com.sensorbergorm.SugarContext;

import net.danlew.android.joda.JodaTimeAndroid;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
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
    ServiceScheduler mServiceScheduler;

    private final Context context;

    @Inject
    @Named("androidBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    private static boolean actionBroadcastReceiversRegistered;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AndroidPlatform(Context context) {
        this.context = context;
        SensorbergApplicationBootstrapper.getComponent().inject(this);

        SugarContext.init(context);
        JodaTimeAndroid.init(context);
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
