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
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.RunLoop;

import android.content.BroadcastReceiver;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mockito.Mockito.spy;

public class TestPlatform implements Platform, HandlerManager {

    public static final String TAG = "TestPlatform";

    @Inject
    Context context;

    @Inject
    @Named("testBluetoothPlatform")
    BluetoothPlatform bluetoothPlatform;

    public CustomClock clock = new CustomClock();

    private Network network;
    private List<NonThreadedRunLoopForTesting> runLoops = new ArrayList<>();

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

    @Override
    public RunLoop getResolverRunLoop(RunLoop.MessageHandlerCallback callback) {
        NonThreadedRunLoopForTesting loop = new NonThreadedRunLoopForTesting(callback, clock);
        runLoops.add(loop);
        return loop;
    }

    @Override
    public RunLoop getBeaconPublisherRunLoop(RunLoop.MessageHandlerCallback callback) {
        NonThreadedRunLoopForTesting loop = new NonThreadedRunLoopForTesting(callback, clock);
        runLoops.add(loop);
        return loop;
    }

    @Override
    public RunLoop getScannerRunLoop(RunLoop.MessageHandlerCallback callback) {
        NonThreadedRunLoopForTesting loop = new NonThreadedRunLoopForTesting(callback, clock);
        runLoops.add(loop);
        return loop;
    }

    public void triggerRunLoop() {
        for (NonThreadedRunLoopForTesting runLoop : runLoops) {
            runLoop.loop();
        }
    }

    public void cleanUp() {

    }

    public class CustomClock implements Clock {
        private long nowInMillis = 0;

        @Override
        public long now() {
            return nowInMillis;
        }

        @Override
        public long elapsedRealtime() {
            return nowInMillis;
        }

        public void setNowInMillis(long nowInMillis) {
            this.nowInMillis = nowInMillis;
            triggerRunLoop();
        }

        public void increaseTimeInMillis(long value) {
            setNowInMillis(nowInMillis + value);
        }
    }
}

