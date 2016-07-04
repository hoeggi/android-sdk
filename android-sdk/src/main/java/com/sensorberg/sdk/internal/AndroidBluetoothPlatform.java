package com.sensorberg.sdk.internal;

import com.sensorberg.bluetooth.CrashCallBackWrapper;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.util.Log;

import javax.inject.Inject;

public class AndroidBluetoothPlatform implements BluetoothPlatform {

    private final CrashCallBackWrapper crashCallBackWrapper;

    private final BluetoothAdapter bluetoothAdapter;

    private boolean leScanRunning = false;

    @Inject
    PermissionChecker permissionChecker;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AndroidBluetoothPlatform(BluetoothAdapter adapter, CrashCallBackWrapper wrapper) {
        crashCallBackWrapper = wrapper;
        bluetoothAdapter = adapter;
    }

    /**
     * Returns a flag indicating whether Bluetooth is enabled.
     *
     * @return a flag indicating whether Bluetooth is enabled
     */
    @Override
    public boolean isBluetoothLowEnergyDeviceTurnedOn() {
        //noinspection SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement
        return isBluetoothLowEnergySupported() && (bluetoothAdapter.isEnabled());
    }

    /**
     * Returns a flag indicating whether Bluetooth is supported.
     *
     * @return a flag indicating whether Bluetooth is supported
     */
    @Override
    public boolean isBluetoothLowEnergySupported() {
        return bluetoothAdapter != null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void startLeScan(BluetoothAdapter.LeScanCallback scanCallback) {
        if (isBluetoothLowEnergySupported()) {
            if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON
                    && permissionChecker.hasScanPermissionCheckAndroid6()) {
                Log.i("bluetooth adapter", Integer.toString(bluetoothAdapter.getState()));
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
        if (isBluetoothLowEnergySupported()) {
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
}
