package com.sensorberg.sdk.scanner;

import android.content.Context;
import android.os.Message;

import com.android.sensorbergVolley.VolleyError;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.internal.Platform;
import com.sensorberg.sdk.internal.RunLoop;
import com.sensorberg.sdk.internal.Transport;
import com.sensorberg.sdk.internal.transport.HistoryCallback;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.settings.Settings;

import java.io.File;
import java.util.List;

public class BeaconActionHistoryPublisher implements ScannerListener, RunLoop.MessageHandlerCallback {

    private static final int MSG_SCAN_EVENT = 2;
    private static final int MSG_MARK_SCANS_AS_SENT =  3;
    private static final int MSG_PUBLISH_HISTORY = 1;
    private static final int MSG_ACTION = 4;
    private static final int MSG_MARK_ACTIONS_AS_SENT = 5;
    private static final int MSG_DELETE_ALL_DATA = 6;
    public static String REALM_FILENAME = "scannerstorage.realm";

    private final RunLoop runloop;
    private final Context context;
    private final Transport transport;
    private final Clock clock;
    private final ResolverListener resolverListener;
    private final Settings settings;

    public BeaconActionHistoryPublisher(Platform plattform, ResolverListener resolverListener, Settings settings) {
        this.resolverListener = resolverListener;
        this.settings = settings;
        transport = plattform.getTransport();
        clock = plattform.getClock();
        runloop = plattform.getBeaconPublisherRunLoop(this);
        context = plattform.getContext();
    }

    @Override
    public void onScanEventDetected(ScanEvent scanEvent) {
        runloop.sendMessage(MSG_SCAN_EVENT, scanEvent);
    }

    @Override
    public void handleMessage(Message queueEvent) {
        long now = clock.now();
        switch (queueEvent.what){
            case MSG_SCAN_EVENT:
                SugarScan.from((ScanEvent) queueEvent.obj, clock.now());
                break;
            case MSG_MARK_SCANS_AS_SENT:
                //noinspection unchecked -> see useage of MSG_MARK_SCANS_AS_SENT
                List<SugarScan> scans = (List<SugarScan>) queueEvent.obj;
                SugarScan.maskAsSent(scans, now, settings.getCacheTtl());
                break;
            case MSG_MARK_ACTIONS_AS_SENT:

                List<SugarAction> actions = (List<SugarAction>) queueEvent.obj;
                SugarAction.markAsSent(actions, now, settings.getCacheTtl());
                break;
            case MSG_PUBLISH_HISTORY:
                publishHistorySynchronously();
                break;
            case MSG_ACTION:
                SugarAction.from((BeaconEvent) queueEvent.obj, clock);
                break;
            case MSG_DELETE_ALL_DATA:
                SugarAction.deleteAll(SugarAction.class);
                SugarScan.deleteAll(SugarScan.class);
                break;
        }
    }
    private void publishHistorySynchronously() {
        List<SugarScan> scans = SugarScan.notSentScans();
        List<SugarAction> actions = SugarAction.notSentScans();
        if (scans.isEmpty() && actions.isEmpty()){
            Logger.log.verbose("nothing to report");
            return;
        }
        transport.publishHistory(scans, actions, new HistoryCallback(){

            @Override
            public void onSuccess(List<SugarScan> scanObjectList, List<SugarAction> actionList){
                runloop.sendMessage(MSG_MARK_SCANS_AS_SENT, scanObjectList);
                runloop.sendMessage(MSG_MARK_ACTIONS_AS_SENT, actionList);
            }

            @Override
            public void onFailure(VolleyError throwable){
                Logger.log.logError("not able to publish history", throwable);
            }

            @Override
            public void onInstantActions(List<BeaconEvent> instantActions) {
                resolverListener.onResolutionsFinished(instantActions);
            }
        });
    }

    public void publishHistory(){
        runloop.add(runloop.obtainMessage(MSG_PUBLISH_HISTORY));
    }

    public void onActionPresented(BeaconEvent beaconEvent) {
        runloop.sendMessage(MSG_ACTION, beaconEvent);
    }

    public void deleteAllObjects() {
        runloop.sendMessage(MSG_DELETE_ALL_DATA);
    }
}
