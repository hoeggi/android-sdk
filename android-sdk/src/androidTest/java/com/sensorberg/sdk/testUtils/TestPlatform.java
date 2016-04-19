package com.sensorberg.sdk.testUtils;

import com.android.sensorbergVolley.Network;
import com.android.sensorbergVolley.RequestQueue;
import com.android.sensorbergVolley.toolbox.BasicNetwork;
import com.android.sensorbergVolley.toolbox.DiskBasedCache;
import com.sensorberg.android.okvolley.OkHttpStack;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.internal.Platform;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;

import android.content.BroadcastReceiver;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mockito.Mockito.spy;

public class TestPlatform implements Platform {

    public static final String TAG = "TestPlatform";

    @Inject
    Context context;

    @Inject
    @Named("testBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    private Network network;

    public TestPlatform() {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    public Network getSpyNetwork(){
        return network;
    }

    public RequestQueue getCachedVolleyQueue() {
        network = spy(new BasicNetwork(new OkHttpStack()));

        File cacheDir = new File(context.getCacheDir(), "volley-test-" + String.valueOf(System.currentTimeMillis()));

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        return queue;
    }

    @Override
    public List<BroadcastReceiver> getBroadcastReceiver() {
        List<BroadcastReceiver> result = new ArrayList<>();
        result.add(new TestBroadcastReceiver());
        return result;
    }

    @Override
    public boolean registerBroadcastReceiver() {
        return false;
    }

    @Override
    public void registerBroadcastReceiver(List<BroadcastReceiver> broadcastReceiver) {
        android.util.Log.e(TAG, "NOT IMPLEMENTED");
    }

    @Override
    public boolean isBluetoothLowEnergySupported() {
        return bluetoothPlatform.isBluetoothLowEnergySupported();
    }

    public void cleanUp() {

    }
}

