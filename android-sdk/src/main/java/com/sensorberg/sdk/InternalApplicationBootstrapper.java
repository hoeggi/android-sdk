package com.sensorberg.sdk;

import com.sensorberg.SensorbergApplication;
import com.sensorberg.android.networkstate.NetworkInfoBroadcastReceiver;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.background.ScannerBroadcastReceiver;
import com.sensorberg.sdk.internal.Platform;
import com.sensorberg.sdk.internal.Transport;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.model.realm.RealmAction;
import com.sensorberg.sdk.presenter.LocalBroadcastManager;
import com.sensorberg.sdk.presenter.ManifestParser;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.Resolution;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;
import com.sensorberg.sdk.resolver.Resolver;
import com.sensorberg.sdk.resolver.ResolverConfiguration;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.scanner.BeaconActionHistoryPublisher;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.Scanner;
import com.sensorberg.sdk.scanner.ScannerListener;
import com.sensorberg.sdk.settings.Settings;
import com.sensorberg.utils.ListUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.realm.Realm;

public class InternalApplicationBootstrapper extends MinimalBootstrapper implements ScannerListener, ResolverListener, Settings.SettingsCallback, Transport.BeaconReportHandler, SyncStatusObserver, Transport.ProximityUUIDUpdateHandler {

    private static final boolean SURVIVE_REBOOT = true;
    final Resolver resolver;
    final Scanner scanner;

    private final Settings settings;

    private final BeaconActionHistoryPublisher beaconActionHistoryPublisher;
    private final Object proximityUUIDsMonitor = new Object();

    private SensorbergService.MessengerList presentationDelegate;
    final Set<String> proximityUUIDs = new HashSet<>();

    @Inject
    Context context;

    @Inject
    Clock clock;

    @Inject
    FileManager fileManager;

    public InternalApplicationBootstrapper(Platform plattform, ServiceScheduler scheduler, HandlerManager handlerManager) {
        super(plattform, scheduler);
        SensorbergApplication.getComponent().inject(this);

        settings = new Settings(plattform);
        settings.restoreValuesFromPreferences();
        settings.setCallback(this);

        plattform.setSettings(settings);

        beaconActionHistoryPublisher = new BeaconActionHistoryPublisher(plattform.getTransport(), this, settings, clock, handlerManager);

        ResolverConfiguration resolverConfiguration = new ResolverConfiguration();

        plattform.getTransport().setBeaconReportHandler(this);
        plattform.getTransport().setProximityUUIDUpdateHandler(this);

        scanner = new Scanner(settings, plattform, settings.shouldRestoreBeaconStates(), clock, fileManager, scheduler, handlerManager);
        resolver = new Resolver(resolverConfiguration, handlerManager, plattform.getTransport());
        scanner.addScannerListener(this);
        resolver.addResolverListener(this);

        serviceScheduler.restorePendingIntents();

        ScannerBroadcastReceiver.setManifestReceiverEnabled(true, context);
        GenericBroadcastReceiver.setManifestReceiverEnabled(true, context);

        setUpAlarmsForSettings();
        setUpAlarmForBeaconActionHistoryPublisher();
        updateAlarmsForActionLayoutFetch();

        //cache the current network state
        NetworkInfoBroadcastReceiver.triggerListenerWithCurrentState(context);
        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS, this);
    }

    private void setUpAlarmForBeaconActionHistoryPublisher() {
        serviceScheduler.scheduleRepeating(SensorbergService.MSG_UPLOAD_HISTORY, settings.getHistoryUploadInterval(), TimeUnit.MILLISECONDS);
    }

    private void setUpAlarmsForSettings(){
        serviceScheduler.scheduleRepeating(SensorbergService.MSG_SETTINGS_UPDATE, settings.getSettingsUpdateInterval(), TimeUnit.MILLISECONDS);
    }

    private void updateAlarmsForActionLayoutFetch(){
        if (platform.isSyncEnabled()) {
            serviceScheduler.scheduleRepeating(SensorbergService.MSG_BEACON_LAYOUT_UPDATE, settings.getLayoutUpdateInterval(), TimeUnit.MILLISECONDS);
        } else{
            serviceScheduler.cancelIntent(SensorbergService.MSG_BEACON_LAYOUT_UPDATE);
        }
    }

    @Override
    public void onScanEventDetected(ScanEvent scanEvent) {
        beaconActionHistoryPublisher.onScanEventDetected(scanEvent);

        boolean contained;
        synchronized (proximityUUIDsMonitor){
            contained = proximityUUIDs.isEmpty() || proximityUUIDs.contains(scanEvent.getBeaconId().getProximityUUIDWithoutDashes());
        }
        if (contained) {
            ResolutionConfiguration resolutionConfiguration = new ResolutionConfiguration();
            resolutionConfiguration.setScanEvent(scanEvent);
            resolutionConfiguration.maxRetries = settings.getMaxRetries();
            resolutionConfiguration.millisBetweenRetries = settings.getMillisBetweenRetries();
            Resolution resolution = resolver.createResolution(resolutionConfiguration);
            resolution.start();
        }
    }

    public void presentBeaconEvent(BeaconEvent beaconEvent) {
        if (beaconEvent != null && beaconEvent.getAction() != null) {
            Action beaconEventAction = beaconEvent.getAction();

            if (beaconEvent.deliverAt != null) {
                serviceScheduler.postDeliverAtOrUpdate(beaconEvent.deliverAt, beaconEvent);
            } else if (beaconEventAction.getDelayTime() > 0) {
                serviceScheduler.postToServiceDelayed(beaconEventAction.getDelayTime(), SensorbergService.GENERIC_TYPE_BEACON_ACTION, beaconEvent,
                        SURVIVE_REBOOT);
                Logger.log.beaconResolveState(beaconEvent, "delaying the display of this BeaconEvent");
            } else {
                presentEventDirectly(beaconEvent);
            }
        }
    }

    private void presentEventDirectly(BeaconEvent beaconEvent) {
        if (beaconEvent.getAction() != null) {
            beaconEvent.setPresentationTime(clock.now());
            beaconActionHistoryPublisher.onActionPresented(beaconEvent);
            if (presentationDelegate == null) {
                Intent broadcastIntent = new Intent(ManifestParser.actionString);
                broadcastIntent.putExtra(Action.INTENT_KEY, beaconEvent.getAction());
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
            } else {
                Logger.log.beaconResolveState(beaconEvent, "delegating the display of the beacon event to the application");
                presentationDelegate.send(beaconEvent);
            }
        }
    }

    public void presentEventDirectly(BeaconEvent beaconEvent, int index) {
        serviceScheduler.removeStoredPendingIntent(index);
        if (beaconEvent != null) {
            presentEventDirectly(beaconEvent);
        }
    }

    @Override
    public void onResolutionFailed(Resolution resolution, Throwable cause) {
        Logger.log.logError("resolution failed:"+ resolution.configuration.getScanEvent().getBeaconId().toTraditionalString() , cause);
    }

    @Override
    public void onResolutionsFinished(List<BeaconEvent> beaconEvents) {
        final Realm realm = Realm.getInstance(context, BeaconActionHistoryPublisher.REALM_FILENAME);
        List<BeaconEvent> events = ListUtils.filter(beaconEvents, new ListUtils.Filter<BeaconEvent>() {
            @Override
            public boolean matches(BeaconEvent beaconEvent) {
                if (beaconEvent.getSuppressionTimeMillis() > 0) {
                    long lastAllowedPresentationTime = clock.now() - beaconEvent.getSuppressionTimeMillis();
                    if (RealmAction.getCountForSuppressionTime(lastAllowedPresentationTime, beaconEvent.getAction().getUuid(), realm)) {
                        return false;
                    }
                }
                if (beaconEvent.sendOnlyOnce) {
                    if (RealmAction.getCountForShowOnlyOnceSuppression(beaconEvent.getAction().getUuid(), realm)) {
                        return false;
                    }

                }
                return true;
            }
        });
        for (BeaconEvent event : events) {
            presentBeaconEvent(event);
        }
    }

    public void sentPresentationDelegationTo(SensorbergService.MessengerList messengerList) {
        presentationDelegate = messengerList;
    }

    public void startScanning() {
        if (platform.isBluetoothLowEnergySupported() && platform.isBluetoothLowEnergyDeviceTurnedOn()){
            scanner.start();
        }
    }

    public void stopScanning() {
        scanner.stop();
    }

    public void hostApplicationInForeground() {
        scanner.hostApplicationInForeground();
        settings.updateValues();
        //we do not care if sync is disabled, the app is in the foreground so we cache!
        platform.getTransport().updateBeaconLayout();
        beaconActionHistoryPublisher.publishHistory();
    }

    public void hostApplicationInBackground() {
        scanner.hostApplicationInBackground();
    }

    public void setApiToken(String apiToken) {
        platform.getTransport().setApiToken(apiToken);
        beaconActionHistoryPublisher.publishHistory();
        if (resolver.configuration.setApiToken(apiToken)){
            unscheduleAllPendingActions();
            beaconActionHistoryPublisher.deleteAllObjects();
        }
    }

    public void updateSettings() {
        settings.updateValues();
    }

    @Override
    public void onSettingsUpdateIntervalChange(Long updateIntervalMillies) {
        serviceScheduler.cancelIntent(SensorbergService.MSG_SETTINGS_UPDATE);
        serviceScheduler.scheduleRepeating(SensorbergService.MSG_SETTINGS_UPDATE, updateIntervalMillies, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onSettingsBeaconLayoutUpdateIntervalChange(long newLayoutUpdateInterval) {
        serviceScheduler.cancelIntent(SensorbergService.MSG_BEACON_LAYOUT_UPDATE);
        serviceScheduler.scheduleRepeating(SensorbergService.MSG_BEACON_LAYOUT_UPDATE, newLayoutUpdateInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onHistoryUploadIntervalChange(long newHistoryUploadInterval) {
        serviceScheduler.cancelIntent(SensorbergService.MSG_UPLOAD_HISTORY);
        serviceScheduler.scheduleRepeating(SensorbergService.MSG_UPLOAD_HISTORY, newHistoryUploadInterval, TimeUnit.MILLISECONDS);
    }

    public void retryScanEventResolve(ResolutionConfiguration retry) {
        this.resolver.retry(retry);
    }

    public void uploadHistory() {
        if (NetworkInfoBroadcastReceiver.latestNetworkInfo != null) {
            beaconActionHistoryPublisher.publishHistory();
        } else {
            Logger.log.logError("Did not try to upload the history because it seems we´e offline.");
        }
    }

    @Override
    public void reportImmediately() {
        beaconActionHistoryPublisher.publishHistory();
    }

    public void updateBeaconLayout() {
        if (platform.isSyncEnabled()) {
            platform.getTransport().updateBeaconLayout();
        }
    }

    @Override
    public void onStatusChanged(int which) {
        updateAlarmsForActionLayoutFetch();
    }

    @Override
    public void proximityUUIDListUpdated(List<String> proximityUUIDs) {
        synchronized (proximityUUIDsMonitor) {
            this.proximityUUIDs.clear();
            for (String proximityUUID : proximityUUIDs) {
                this.proximityUUIDs.add(proximityUUID.toLowerCase());
            }
        }
    }
}
