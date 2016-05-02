package com.sensorberg.sdk;

import com.sensorberg.SensorbergApplicationBootstrapper;
import com.sensorberg.android.networkstate.NetworkInfoBroadcastReceiver;
import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.background.ScannerBroadcastReceiver;
import com.sensorberg.sdk.internal.PermissionChecker;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.MessageDelayWindowLengthListener;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
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
import com.sensorberg.sdk.settings.SettingsManager;
import com.sensorberg.sdk.settings.SettingsUpdateCallback;
import com.sensorberg.utils.ListUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncStatusObserver;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;

public class InternalApplicationBootstrapper extends MinimalBootstrapper
        implements ScannerListener, Transport.BeaconReportHandler, SyncStatusObserver, Transport.ProximityUUIDUpdateHandler {

    private static final boolean SURVIVE_REBOOT = true;

    final Transport transport;

    final Resolver resolver;

    final Scanner scanner;

    @Inject
    @Named("realSettingsManager")
    SettingsManager settingsManager;

    @Inject
    @Named("realBeaconActionHistoryPublisher")
    BeaconActionHistoryPublisher beaconActionHistoryPublisher;

    private final Object proximityUUIDsMonitor = new Object();

    private SensorbergService.MessengerList presentationDelegate;

    final Set<String> proximityUUIDs = new HashSet<>();

    @Inject
    Context context;

    Clock clock;

    @Inject
    FileManager fileManager;

    @Inject
    ServiceScheduler mServiceScheduler;

    @Inject
    PermissionChecker permissionChecker;

    BluetoothPlatform bluetoothPlatform;

    public InternalApplicationBootstrapper(Transport transport, ServiceScheduler scheduler, HandlerManager handlerManager,
            Clock clk, BluetoothPlatform btPlatform, SharedPreferences preferences) {
        super(scheduler);
        SensorbergApplicationBootstrapper.getComponent().inject(this);

        this.transport = transport;
        settingsManager.setSettingsUpdateCallback(settingsUpdateCallbackListener);
        settingsManager.setMessageDelayWindowLengthListener((MessageDelayWindowLengthListener) mServiceScheduler);
        clock = clk;
        bluetoothPlatform = btPlatform;

        beaconActionHistoryPublisher.setResolverListener(resolverListener);

        ResolverConfiguration resolverConfiguration = new ResolverConfiguration();

        scanner = new Scanner(settingsManager, settingsManager.isShouldRestoreBeaconStates(), clock, fileManager, scheduler, handlerManager,
                btPlatform);
        resolver = new Resolver(resolverConfiguration, handlerManager, transport);
        scanner.addScannerListener(this);
        resolver.addResolverListener(resolverListener);

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
        serviceScheduler.scheduleRepeating(SensorbergService.MSG_UPLOAD_HISTORY, settingsManager.getHistoryUploadInterval(), TimeUnit.MILLISECONDS);
    }

    private void setUpAlarmsForSettings() {
        serviceScheduler.scheduleRepeating(SensorbergService.MSG_SETTINGS_UPDATE, settingsManager.getSettingsUpdateInterval(), TimeUnit.MILLISECONDS);
    }

    private void updateAlarmsForActionLayoutFetch() {
        if (isSyncEnabled()) {
            serviceScheduler
                    .scheduleRepeating(SensorbergService.MSG_BEACON_LAYOUT_UPDATE, settingsManager.getLayoutUpdateInterval(), TimeUnit.MILLISECONDS);
        } else {
            serviceScheduler.cancelIntent(SensorbergService.MSG_BEACON_LAYOUT_UPDATE);
        }
    }

    @Override
    public void onScanEventDetected(ScanEvent scanEvent) {
        beaconActionHistoryPublisher.onScanEventDetected(scanEvent);

        boolean contained;
        synchronized (proximityUUIDsMonitor) {
            contained = proximityUUIDs.isEmpty() || proximityUUIDs.contains(scanEvent.getBeaconId().getProximityUUIDWithoutDashes());
        }
        if (contained) {
            ResolutionConfiguration resolutionConfiguration = new ResolutionConfiguration();
            resolutionConfiguration.setScanEvent(scanEvent);
            resolutionConfiguration.maxRetries = settingsManager.getMaxRetries();
            resolutionConfiguration.millisBetweenRetries = settingsManager.getMillisBetweenRetries();
            Resolution resolution = resolver.createResolution(resolutionConfiguration);
            resolution.start();
        }
    }

    //TODO what is with this method having no access modifier.
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

    public void sentPresentationDelegationTo(SensorbergService.MessengerList messengerList) {
        presentationDelegate = messengerList;
    }

    public void startScanning() {
        if (bluetoothPlatform.isBluetoothLowEnergySupported() && bluetoothPlatform.isBluetoothLowEnergyDeviceTurnedOn()) {
            scanner.start();
        }
    }

    public void stopScanning() {
        scanner.stop();
    }

    public void hostApplicationInForeground() {
        scanner.hostApplicationInForeground();
        updateSettings();
        //we do not care if sync is disabled, the app is in the foreground so we cache!
        transport.updateBeaconLayout();
        beaconActionHistoryPublisher.publishHistory();
    }

    public void hostApplicationInBackground() {
        scanner.hostApplicationInBackground();
    }

    public void setApiToken(String apiToken) {
        transport.setApiToken(apiToken);
        beaconActionHistoryPublisher.publishHistory();
        if (resolver.configuration.setApiToken(apiToken)) {
            unscheduleAllPendingActions();
            beaconActionHistoryPublisher.deleteAllObjects();
        }
    }

    public void updateSettings() {
        settingsManager.updateSettingsFromNetwork();
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

    @SuppressWarnings("SimplifiableIfStatement")
    boolean isSyncEnabled() {
        if (permissionChecker.hasReadSyncSettingsPermissions()) {
            return ContentResolver.getMasterSyncAutomatically();
        } else {
            return true;
        }
    }

    @Override
    public void reportImmediately() {
        beaconActionHistoryPublisher.publishHistory();
    }

    public void updateBeaconLayout() {
        if (isSyncEnabled()) {
            transport.updateBeaconLayout();
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

    @Getter
    ResolverListener resolverListener = new ResolverListener() {
        @Override
        public void onResolutionFailed(Resolution resolution, Throwable cause) {
            Logger.log.logError("resolution failed:" + resolution.configuration.getScanEvent().getBeaconId().toTraditionalString(), cause);
        }

        @Override
        public void onResolutionsFinished(List<BeaconEvent> beaconEvents) {
            List<BeaconEvent> events = ListUtils.filter(beaconEvents, new ListUtils.Filter<BeaconEvent>() {
                @Override
                public boolean matches(BeaconEvent beaconEvent) {
                    if (beaconEvent.getSuppressionTimeMillis() > 0) {
                        long lastAllowedPresentationTime = clock.now() - beaconEvent.getSuppressionTimeMillis();
                        if (SugarAction.getCountForSuppressionTime(lastAllowedPresentationTime, beaconEvent.getAction().getUuid())) {
                            return false;
                        }
                    }
                    if (beaconEvent.sendOnlyOnce) {
                        Log.i("this", "sendOnlyOnce");
                        System.out.print("sendOnlyOnce");
                        if (SugarAction.getCountForShowOnlyOnceSuppression(beaconEvent.getAction().getUuid())) {
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
    };

    SettingsUpdateCallback settingsUpdateCallbackListener = new SettingsUpdateCallback() {
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
    };
}
