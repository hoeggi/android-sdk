package com.sensorberg.sdk;

import com.sensorberg.SensorbergApplicationBootstrapper;
import com.sensorberg.sdk.internal.URLFactory;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.Platform;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.internal.transport.interfaces.Transport;
import com.sensorberg.sdk.receivers.GenericBroadcastReceiver;
import com.sensorberg.sdk.receivers.ScannerBroadcastReceiver;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;
import com.sensorberg.sdk.resolver.ResolverConfiguration;

import net.danlew.android.joda.JodaTimeAndroid;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.Toast;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

@SuppressWarnings({"WeakerAccess", "pmd:TooManyMethods", "squid:S1200"})
public class SensorbergService extends Service {

    @Inject
    protected FileManager fileManager;

    @Inject
    protected ServiceScheduler serviceScheduler;

    @Inject
    @Named("realHandlerManager")
    protected HandlerManager handlerManager;

    @Inject
    @Named("realClock")
    protected Clock clock;

    @Inject
    @Named("androidBluetoothPlatform")
    protected BluetoothPlatform bluetoothPlatform;

    @Inject
    @Named("realTransport")
    protected Transport transport;

    @Inject
    @Named("androidPlatformIdentifier")
    protected PlatformIdentifier platformIdentifier;

    @Inject
    @Named("androidPlatform")
    protected Platform platform;

    private final MessengerList presentationDelegates = new MessengerList();

    protected InternalApplicationBootstrapper bootstrapper;

    @Override
    public void onCreate() {
        super.onCreate();
        SensorbergApplicationBootstrapper.getComponent().inject(this);
        Logger.log.logServiceState("onCreate");
        JodaTimeAndroid.init(this);
    }

    @SuppressWarnings("checkstyle:com.puppycrawl.tools.checkstyle.checks.metrics.NPathComplexityCheck")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.log.logServiceState("onStartCommand");

        if (!bluetoothPlatform.isBluetoothLowEnergySupported()) {
            Logger.log.logError("isBluetoothLowEnergySupported not true, shutting down.");
            return stopSensorbergService();
        }

        if (!platform.registerBroadcastReceiver()) {
            Logger.log.logError("no BroadcastReceiver registered for Action:com.sensorberg.android.PRESENT_ACTION");
            return stopSensorbergService();
        }

        if (intent == null) {
            return restartSensorbergService();
        } else {
            return handleIntent(intent);
        }
    }

    protected int handleIntent(Intent intent) {
        Logger.log.serviceHandlesMessage(
                SensorbergServiceMessage.stringFrom(intent.getIntExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, -1)));

        handleDebuggingIntent(intent, this);

        if (handleIntentEvenIfNoBootstrapperPresent(intent)) {
            return stopSensorbergService();
        }

        if (bootstrapper == null) {
            updateDiskConfiguration(intent);
        }

        if (intent.hasExtra(SensorbergServiceMessage.EXTRA_START_SERVICE)) {
            return startSensorbergService(intent.getStringExtra(SensorbergServiceMessage.EXTRA_API_KEY));
        }

        if (intent.hasExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE)) {
            if (bootstrapper == null) {
                bootstrapper = createBootstrapperFromDiskConfiguration();
                if (bootstrapper == null) {
                    Logger.log.logError("couldn't start the SDK!");
                    return stopSensorbergService();
                }
            }

            handleIntentMessage(intent);
        }

        return START_STICKY;
    }

    protected int startSensorbergService(String apiKey) {
        if (bootstrapper == null && (!TextUtils.isEmpty(apiKey))) {
            bootstrapper = createBootstrapper(apiKey);
            persistConfiguration(bootstrapper.resolver.configuration);
            bootstrapper.startScanning();
            return START_STICKY;
        } else if (bootstrapper != null) {
            bootstrapper.startScanning();
            Logger.log.logError("start intent was sent, but the scanner was already set up");
            return START_STICKY;
        } else {
            Logger.log.logError("Intent to start the service was not correctly sent. not starting the service");
            return stopSensorbergService();
        }
    }

    protected int restartSensorbergService() {
        Logger.log.logError("there was no intent in onStartCommand we must assume we are beeing restarted due to a kill event");
        bootstrapper = createBootstrapperFromDiskConfiguration();
        if (bootstrapper != null) {
            bootstrapper.startScanning();
        }
        return START_STICKY;
    }

    protected int stopSensorbergService() {
        stopSelf();
        return START_NOT_STICKY;
    }

    protected void handleDebuggingIntent(Intent intent, Context context) {
        switch (intent.getIntExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, -1)) {
            case SensorbergServiceMessage.MSG_TYPE_DISABLE_LOGGING: {
                Logger.log = Logger.QUIET_LOG;
                transport.setLoggingEnabled(false);
                Toast.makeText(context, "Log disabled " + context.getPackageName(), Toast.LENGTH_SHORT).show();
                break;
            }
            case SensorbergServiceMessage.MSG_TYPE_ENABLE_LOGGING: {
                Logger.enableVerboseLogging();
                transport.setLoggingEnabled(true);
                Toast.makeText(context, "Log enabled " + context.getPackageName(), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    protected SensorbergServiceConfiguration loadOrCreateNewServiceConfiguration(FileManager fileManager) {
        SensorbergServiceConfiguration diskConf = SensorbergServiceConfiguration.loadFromDisk(fileManager);

        if (diskConf == null) {
            diskConf = new SensorbergServiceConfiguration(new ResolverConfiguration());
        } else if (diskConf.resolverConfiguration == null) {
            diskConf.resolverConfiguration = new ResolverConfiguration();
        }

        return diskConf;
    }

    protected void updateDiskConfiguration(Intent intent) {
        if (intent.hasExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE)) {
            int type = intent.getIntExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, -1);
            SensorbergServiceConfiguration diskConf = loadOrCreateNewServiceConfiguration(fileManager);

            Logger.log.serviceHandlesMessage(SensorbergServiceMessage.stringFrom(type));

            switch (type) {
                case SensorbergServiceMessage.MSG_TYPE_SET_RESOLVER_ENDPOINT: {
                    if (intent.hasExtra(SensorbergServiceMessage.MSG_SET_RESOLVER_ENDPOINT_ENDPOINT_URL)) {
                        URL resolverURL = (URL) intent.getSerializableExtra(SensorbergServiceMessage.MSG_SET_RESOLVER_ENDPOINT_ENDPOINT_URL);
                        diskConf.resolverConfiguration.setResolverLayoutURL(resolverURL);
                        URLFactory.setLayoutURL(diskConf.resolverConfiguration.getResolverLayoutURL().toString());
                    }
                    break;
                }
                case SensorbergServiceMessage.MSG_SET_API_TOKEN: {
                    if (intent.hasExtra(SensorbergServiceMessage.MSG_SET_API_TOKEN_TOKEN)) {
                        String apiToken = intent.getStringExtra(SensorbergServiceMessage.MSG_SET_API_TOKEN_TOKEN);
                        diskConf.resolverConfiguration.setApiToken(apiToken);
                    }
                    break;
                }
                case SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER: {
                    if (intent.hasExtra(SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER_ADVERTISING_IDENTIFIER)) {
                        String advertisingIdentifier = intent.getStringExtra(
                                SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER_ADVERTISING_IDENTIFIER);
                        diskConf.resolverConfiguration.setAdvertisingIdentifier(advertisingIdentifier);
                    }
                    break;
                }
            }

            diskConf.writeToDisk(fileManager);
        }
    }

    protected boolean handleIntentEvenIfNoBootstrapperPresent(Intent intent) {
        if (intent.hasExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE)) {
            int type = intent.getIntExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, -1);
            switch (type) {
                case SensorbergServiceMessage.MSG_SHUTDOWN: {
                    Logger.log.serviceHandlesMessage(SensorbergServiceMessage.stringFrom(type));
                    MinimalBootstrapper minimalBootstrapper = bootstrapper != null ? bootstrapper : new MinimalBootstrapper(serviceScheduler);
                    SensorbergServiceConfiguration.removeConfigurationFromDisk(fileManager);
                    ScannerBroadcastReceiver.setManifestReceiverEnabled(false, this);
                    GenericBroadcastReceiver.setManifestReceiverEnabled(false, this);

                    minimalBootstrapper.unscheduleAllPendingActions();
                    minimalBootstrapper.stopScanning();
                    minimalBootstrapper.stopAllScheduledOperations();
                    bootstrapper = null;
                    return true;
                }
            }
        }
        return false;
    }

    protected InternalApplicationBootstrapper createBootstrapperFromDiskConfiguration() {
        InternalApplicationBootstrapper newBootstrapper = null;

        try {
            SensorbergServiceConfiguration diskConf = SensorbergServiceConfiguration.loadFromDisk(fileManager);

            //TODO is this a viable case, that we have resolver url but no api key? we're not creating a bootstrapper in first case
            if (diskConf != null && diskConf.resolverConfiguration.getResolverLayoutURL() != null) {
                URLFactory.setLayoutURL(diskConf.resolverConfiguration.getResolverLayoutURL().toString());
            }
            if (diskConf != null && diskConf.isComplete()) {
                newBootstrapper = createBootstrapper(diskConf.resolverConfiguration.apiToken);
            } else {
                Logger.log.logError("configuration from disk could not be loaded or is not complete");
            }
        } catch (Exception e) {
            Logger.log.logError("something went wrong when loading the configuration from disk", e);
        }

        return newBootstrapper;
    }

    private InternalApplicationBootstrapper createBootstrapper(String apikey) {
        InternalApplicationBootstrapper newBootstrapper = new InternalApplicationBootstrapper(transport, serviceScheduler, handlerManager, clock,
                bluetoothPlatform);
        newBootstrapper.setApiToken(apikey);

        return newBootstrapper;
    }

    private void persistConfiguration(ResolverConfiguration resolverConfiguration) {
        SensorbergServiceConfiguration conf = new SensorbergServiceConfiguration(resolverConfiguration);
        conf.writeToDisk(fileManager);
    }

    protected void handleIntentMessage(Intent intent) {
        int what = intent.getIntExtra(SensorbergServiceMessage.EXTRA_GENERIC_TYPE, -1);
        Logger.log.serviceHandlesMessage(SensorbergServiceMessage.stringFrom(what));

        switch (what) {
            case SensorbergServiceMessage.MSG_BEACON_LAYOUT_UPDATE:
                bootstrapper.updateBeaconLayout();
                break;
            case SensorbergServiceMessage.MSG_SDK_SCANNER_MESSAGE:
                Bundle message = intent.getParcelableExtra(SensorbergServiceMessage.EXTRA_GENERIC_WHAT);
                bootstrapper.scanner.handlePlatformMessage(message);
                break;
            case SensorbergServiceMessage.MSG_SETTINGS_UPDATE:
                bootstrapper.updateSettings();
                break;
            case SensorbergServiceMessage.MSG_UPLOAD_HISTORY:
                bootstrapper.uploadHistory();
                break;
            case SensorbergServiceMessage.GENERIC_TYPE_BEACON_ACTION: {
                presentBeaconEvent(intent);
                break;
            }
            case SensorbergServiceMessage.GENERIC_TYPE_RETRY_RESOLVE_SCANEVENT: {
                ResolutionConfiguration configuration = intent.getParcelableExtra(SensorbergServiceMessage.EXTRA_GENERIC_WHAT);
                bootstrapper.retryScanEventResolve(configuration);
                break;
            }
            case SensorbergServiceMessage.MSG_APPLICATION_IN_FOREGROUND: {
                bootstrapper.hostApplicationInForeground();
                break;
            }
            case SensorbergServiceMessage.MSG_APPLICATION_IN_BACKGROUND: {
                bootstrapper.hostApplicationInBackground();
                break;
            }
            case SensorbergServiceMessage.MSG_SET_API_TOKEN: {
                setApiToken(intent);
                break;
            }
            case SensorbergServiceMessage.MSG_TYPE_SET_RESOLVER_ENDPOINT: {
                setResolverEndpoint(intent);
                break;
            }
            case SensorbergServiceMessage.MSG_REGISTER_PRESENTATION_DELEGATE: {
                registerPresentationDelegate(intent);
                break;
            }
            case SensorbergServiceMessage.MSG_UNREGISTER_PRESENTATION_DELEGATE: {
                unregisterPresentationDelegate(intent);
                break;
            }
            case SensorbergServiceMessage.MSG_PING: {
                bootstrapper.startScanning();
                break;
            }
            case SensorbergServiceMessage.MSG_BLUETOOTH: {
                processBluetoothStateMessage(intent);
                break;
            }
            case SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER: {
                setAdvertisingIdentifier(intent);
                break;
            }
        }
    }

    protected void presentBeaconEvent(Intent intent) {
        try {
            BeaconEvent beaconEvent = intent.getParcelableExtra(SensorbergServiceMessage.EXTRA_GENERIC_WHAT);
            int index = intent.getIntExtra(SensorbergServiceMessage.EXTRA_GENERIC_INDEX, 0);
            Logger.log.beaconResolveState(beaconEvent, "end of the delay, now showing the BeaconEvent");
            bootstrapper.presentEventDirectly(beaconEvent, index);
        } catch (Exception e) {
            Logger.log.logError("Problem showing BeaconEvent: " + e.getMessage());
        }
    }

    protected void setApiToken(Intent intent) {
        if (intent.hasExtra(SensorbergServiceMessage.MSG_SET_API_TOKEN_TOKEN)) {
            String apiToken = intent.getStringExtra(SensorbergServiceMessage.MSG_SET_API_TOKEN_TOKEN);
            bootstrapper.setApiToken(apiToken);
            persistConfiguration(bootstrapper.resolver.configuration);
        }
    }

    protected void setResolverEndpoint(Intent intent) {
        if (intent.hasExtra(SensorbergServiceMessage.MSG_SET_RESOLVER_ENDPOINT_ENDPOINT_URL)) {
            try {
                URL resolverURL = (URL) intent.getSerializableExtra(SensorbergServiceMessage.MSG_SET_RESOLVER_ENDPOINT_ENDPOINT_URL);
                URLFactory.setLayoutURL(resolverURL.toString());
            } catch (Exception e) {
                Logger.log
                        .logError("Could not parse the extra " + SensorbergServiceMessage.MSG_SET_RESOLVER_ENDPOINT_ENDPOINT_URL, e);
            }
        }
    }

    protected void registerPresentationDelegate(Intent intent) {
        if (intent.hasExtra(SensorbergServiceMessage.EXTRA_MESSENGER)) {
            Messenger messenger = intent.getParcelableExtra(SensorbergServiceMessage.EXTRA_MESSENGER);
            presentationDelegates.add(messenger);
        }
    }

    protected void unregisterPresentationDelegate(Intent intent) {
        if (intent.hasExtra(SensorbergServiceMessage.EXTRA_MESSENGER)) {
            Messenger messenger = intent.getParcelableExtra(SensorbergServiceMessage.EXTRA_MESSENGER);
            presentationDelegates.remove(messenger);
        }
    }

    protected void processBluetoothStateMessage(Intent intent) {
        if (intent.hasExtra(SensorbergServiceMessage.EXTRA_BLUETOOTH_STATE)) {
            boolean bluetoothOn = intent.getBooleanExtra(SensorbergServiceMessage.EXTRA_BLUETOOTH_STATE, true);
            if (bluetoothOn) {
                bootstrapper.startScanning();
            } else {
                bootstrapper.stopScanning();
            }
        }
    }

    protected void setAdvertisingIdentifier(Intent intent) {
        if (intent.hasExtra(SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER_ADVERTISING_IDENTIFIER)) {
            String advertisingIdentifier = intent.getStringExtra(
                    SensorbergServiceMessage.MSG_SET_API_ADVERTISING_IDENTIFIER_ADVERTISING_IDENTIFIER);
            platformIdentifier.setAdvertisingIdentifier(advertisingIdentifier);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.log.logServiceState("onUnbind");
        return false;
    }

    @Override
    public void onDestroy() {
        Logger.log.logServiceState("onDestroy");
        if (bootstrapper != null) {
            bootstrapper.stopScanning();
        }
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Logger.log.logServiceState("onTaskRemoved");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MessengerList {

        private final Set<Messenger> storage = new HashSet<>();

        public void add(Messenger replyTo) {
            storage.clear();
            storage.add(replyTo);
            if (storage.size() >= 1) {
                bootstrapper.sentPresentationDelegationTo(this);
            }
        }

        public void remove(Messenger replyTo) {
            storage.remove(replyTo);
            storage.clear();
            if (storage.size() == 0) {
                bootstrapper.sentPresentationDelegationTo(null);
            }
        }

        public void send(BeaconEvent beaconEvent) {
            for (Messenger messenger : storage) {
                try {
                    Message message = Message.obtain(null, SensorbergServiceMessage.MSG_PRESENT_ACTION);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(SensorbergServiceMessage.MSG_PRESENT_ACTION_BEACONEVENT, beaconEvent);
                    message.setData(bundle);
                    messenger.send(message);
                } catch (DeadObjectException d) {
                    //we need to remove this object!!
                } catch (RemoteException e) {
                    Logger.log.logError("something went wrong sending BeaconEvent through Messenger", e);
                }
            }
        }
    }
}
