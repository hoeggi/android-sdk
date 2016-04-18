package com.sensorberg.sdk.internal;

import com.sensorberg.SensorbergApplication;
import com.sensorberg.android.okvolley.OkVolley;
import com.sensorberg.bluetooth.CrashCallBackWrapper;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.presenter.LocalBroadcastManager;
import com.sensorberg.sdk.presenter.ManifestParser;
import com.sensorberg.sdk.settings.Settings;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
    CrashCallBackWrapper crashCallBackWrapper;

    @Inject
    @Named("androidPlatformIdentifier")
    PlatformIdentifier platformIdentifier;

    private final BluetoothAdapter bluetoothAdapter;

    private final boolean bluetoothLowEnergySupported;

    private Transport asyncTransport;

    private boolean leScanRunning = false;

    private Settings settings;

    private boolean shouldUseHttpCache = true;

    private static boolean actionBroadcastReceiversRegistered;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AndroidPlatform(Context context) {
        this.context = context;
        SensorbergApplication.getComponent().inject(this);

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothLowEnergySupported = true;
        } else {
            bluetoothLowEnergySupported = false;
            bluetoothAdapter = null;
        }
    }

    @Override
    public Transport getTransport() {
        if (asyncTransport == null) {
            asyncTransport = new OkHttpClientTransport(this, settings, OkVolley.newRequestQueue(context, shouldUseHttpCache), clock,
                    platformIdentifier);
        }
        return asyncTransport;
    }

    @Override
    public boolean useSyncClient() {
        return false;
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
    public boolean hasMinimumAndroidRequirements() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
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
    public void setSettings(Settings settings) {
        this.settings = settings;
        mServiceScheduler.setSettings(settings);
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

    /**
     * Returns a flag indicating whether Bluetooth is enabled.
     *
     * @return a flag indicating whether Bluetooth is enabled
     */
    @Override
    public boolean isBluetoothLowEnergyDeviceTurnedOn() {
        //noinspection SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement
        return bluetoothLowEnergySupported && (bluetoothAdapter.isEnabled());
    }

    /**
     * Returns a flag indicating whether Bluetooth is supported.
     *
     * @return a flag indicating whether Bluetooth is supported
     */
    @Override
    public boolean isBluetoothLowEnergySupported() {
        return bluetoothLowEnergySupported;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void startLeScan(BluetoothAdapter.LeScanCallback scanCallback) {
        if (bluetoothLowEnergySupported) {
            if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                //noinspection deprecation old API compatability
                bluetoothAdapter.startLeScan(crashCallBackWrapper);
                crashCallBackWrapper.setCallback(scanCallback);
                leScanRunning = true;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void stopLeScan() {
        if (bluetoothLowEnergySupported) {
            try {
                //noinspection deprecation old API compatability
                bluetoothAdapter.stopLeScan(crashCallBackWrapper);
            } catch (NullPointerException sentBySysteminternally) {
                Logger.log.logError("System bug throwing a NullPointerException internally.", sentBySysteminternally);
            } finally {
                leScanRunning = false;
                crashCallBackWrapper.setCallback(null);
            }
        }
    }

    @Override
    public boolean isLeScanRunning() {
        return leScanRunning;
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public boolean isBluetoothEnabled() {
        return bluetoothLowEnergySupported && bluetoothAdapter.isEnabled();
    }

}
