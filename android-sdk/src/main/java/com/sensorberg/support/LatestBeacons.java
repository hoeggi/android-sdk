package com.sensorberg.support;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.model.BeaconId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by falkorichter on 07.12.15.
 */
public class LatestBeacons {

    static class LopperThread extends HandlerThread {

        final CountDownLatch latch;
        Incominghandler handler;

        LopperThread(CountDownLatch latch) {
            super("LatestBeacons thread");
            this.latch = latch;
        }

        public synchronized void waitUntilReady() {
            handler = new Incominghandler(getLooper(), this.latch);
        }
    }

    static class Incominghandler extends Handler {
        private final CountDownLatch latch;
        public ArrayList<BeaconId> beaconIds;

        public Incominghandler(Looper looper, CountDownLatch latch) {
            super(looper);
            this.latch = latch;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SensorbergService.MSG_LIST_OF_BEACONS:
                    Bundle bundle = msg.getData();
                    bundle.setClassLoader(BeaconId.class.getClassLoader());
                    beaconIds = bundle.getParcelableArrayList("beaconIds");
                    latch.countDown();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public static Collection<BeaconId> getLatestBeacons(Context context, long duration, TimeUnit unit) {
        return getLatestBeacons(context, duration, unit, 5, TimeUnit.SECONDS);
    }

    public static Collection<BeaconId> getLatestBeacons(Context context,
                                                        long duration, TimeUnit unit,
                                                        long timeoutduration, TimeUnit timeoutTimeUnit){
        if(Looper.getMainLooper() == Looper.myLooper()){
            throw new IllegalArgumentException("Calling this from your main thread can lead to deadlock");
        }



        CountDownLatch latch = new CountDownLatch(1);
        LopperThread thread = new LopperThread(latch);
        thread.start();
        thread.waitUntilReady();

        Messenger messenger = new Messenger(thread.handler);
        Intent intent = new Intent(context, SensorbergService.class);
        intent.putExtra("messenger", messenger);
        intent.putExtra("milliseconds", unit.toMillis(duration));
        intent.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_LIST_OF_BEACONS);

        context.startService(intent);
        try {
            if (latch.await(timeoutduration, timeoutTimeUnit)){
                thread.quit();
                return thread.handler.beaconIds;
            }
            return Collections.EMPTY_LIST;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }
}
