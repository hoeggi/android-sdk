package com.sensorberg.support;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.model.BeaconId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LatestBeacons {

    //inspired by http://stackoverflow.com/questions/4838207/how-to-create-a-looper-thread-then-send-it-a-message-immediately
    static class LooperThread extends HandlerThread {

        final CountDownLatch latch;
        Incominghandler handler;

        LooperThread(CountDownLatch latch) {
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
                    beaconIds = bundle.getParcelableArrayList(SensorbergService.MSG_LIST_OF_BEACONS_BEACON_IDS);
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

        LooperThread thread = new LooperThread(new CountDownLatch(1));
        thread.start();
        thread.waitUntilReady();

        Messenger messenger = new Messenger(thread.handler);
        Intent intent = new Intent(context, SensorbergService.class);
        intent.putExtra(SensorbergService.MSG_LIST_OF_BEACONS_MESSENGER, messenger);
        intent.putExtra(SensorbergService.MSG_LIST_OF_BEACONS_MILLIS, unit.toMillis(duration));
        intent.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_LIST_OF_BEACONS);

        context.startService(intent);
        try {
            if (thread.latch.await(timeoutduration, timeoutTimeUnit)){
                thread.quit();
                return thread.handler.beaconIds;
            }
            throw new TimeoutException("The inter process communication timed out. Timeout was set to " + timeoutTimeUnit.toMillis(duration) + "ms");
        } catch (Exception e){
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }
}
