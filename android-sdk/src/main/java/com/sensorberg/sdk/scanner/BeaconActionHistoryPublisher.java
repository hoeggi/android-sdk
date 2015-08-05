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
import com.sensorberg.sdk.model.realm.RealmAction;
import com.sensorberg.sdk.model.realm.RealmScan;
import com.sensorberg.sdk.realm.migrations.Version0to1Migration;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.settings.Settings;

import java.io.File;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;

public class BeaconActionHistoryPublisher implements ScannerListener, RunLoop.MessageHandlerCallback {

    private static final int MSG_SCAN_EVENT = 2;
    private static final int MSG_MARK_SCANS_AS_SENT =  3;
    private static final int MSG_PUBLISH_HISTORY = 1;
    private static final int MSG_ACTION = 4;
    private static final int MSG_MARK_ACTIONS_AS_SENT = 5;
    public static String REALM_FILENAME = "scannerstorage.realm";

    private final RunLoop runloop;
    private final Context context;
    private final Transport transport;
    private final Clock clock;
    private final ResolverListener resolverListener;
    private final Settings settings;
    private Realm realm;



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
        if (realm == null) {
            try {
                realm = Realm.getInstance(context, REALM_FILENAME);
            } catch (RealmMigrationNeededException e) {
                Logger.log.logServiceState("database migration needed");
                Realm.migrateRealmAtPath(new File(context.getFilesDir(), REALM_FILENAME).getPath(), new Version0to1Migration(), false);
                Logger.log.logServiceState("database migration complete, opening realm again");
                realm = Realm.getInstance(context, REALM_FILENAME);
                Logger.log.logServiceState("realm opened successfully");
            }
        }
        long now = clock.now();
        switch (queueEvent.what){
            case MSG_SCAN_EVENT:
                realm.beginTransaction();
                RealmScan.from((ScanEvent) queueEvent.obj, realm, clock.now());
                realm.commitTransaction();
                break;
            case MSG_MARK_SCANS_AS_SENT:
                //noinspection unchecked
                List<RealmScan> scans = (RealmResults<RealmScan>) queueEvent.obj;
                RealmScan.maskAsSent(scans, realm, now, settings.getCacheTtl());
                break;
            case MSG_MARK_ACTIONS_AS_SENT:
                //noinspection unchecked
                List<RealmAction> actions = (List<RealmAction>) queueEvent.obj;
                RealmAction.markAsSent(actions, realm, now, settings.getCacheTtl());
                break;
            case MSG_PUBLISH_HISTORY:
                publishHistorySynchronously();
                break;
            case MSG_ACTION:
                realm.beginTransaction();
                RealmAction.from((BeaconEvent) queueEvent.obj, realm, clock);
                realm.commitTransaction();
                break;

        }
    }
    private void publishHistorySynchronously() {
        RealmResults<RealmScan> scans = RealmScan.notSentScans(realm);
        RealmResults<RealmAction> actions = RealmAction.notSentScans(realm);
        if (scans.isEmpty() && actions.isEmpty()){
            Logger.log.verbose("nothing to report");
            return;
        }
        transport.publishHistory(scans, actions, new HistoryCallback(){

            @Override
            public void onSuccess(List<RealmScan> scanObjectList, List<RealmAction> actionList){
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
}
