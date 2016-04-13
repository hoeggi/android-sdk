package com.sensorberg.sdk.internal;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.sensorberg.SensorbergApplication;
import com.sensorberg.android.okvolley.OkVolley;
import com.sensorberg.bluetooth.CrashCallBackWrapper;
import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.GenericBroadcastReceiver;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.presenter.LocalBroadcastManager;
import com.sensorberg.sdk.presenter.ManifestParser;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.settings.Settings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static com.sensorberg.utils.UUIDUtils.uuidWithoutDashesString;

public class AndroidPlatform implements Platform {

    private static final String SENSORBERG_PREFERENCE_INSTALLATION_IDENTIFIER = "com.sensorberg.preferences.installationUuidIdentifier";
    private static final String SENSORBERG_PREFERENCE_ADVERTISER_IDENTIFIER = "com.sensorberg.preferences.advertiserIdentifier";

    @Inject
    SharedPreferences settingsPreferences;

    @Inject
    Clock clock;

    private final Context context;


    private CrashCallBackWrapper crashCallBackWrapper;
    private final BluetoothAdapter bluetoothAdapter;
    private final boolean bluetoothLowEnergySupported;
    private String userAgentString;
    private Transport asyncTransport;

    private String deviceInstallationIdentifier;
    private String advertiserIdentifier;

    private boolean leScanRunning = false;
    private final Set<Integer> repeatingPendingIntents = new HashSet<>();

    private final PersistentIntegerCounter postToServiceCounter;
    private final PendingIntentStorage pendingIntentStorage;
    private Settings settings;
    Class<? extends BroadcastReceiver> genericBroadcastReceiverClass = GenericBroadcastReceiver.class;
    private boolean shouldUseHttpCache = true;
    private static boolean actionBroadcastReceiversRegistered;
    private final PermissionChecker permissionChecker;
    private  final ArrayList<DeviceInstallationIdentifierChangeListener> deviceInstallationIdentifierChangeListener = new ArrayList<>();
    private  final ArrayList<AdvertiserIdentifierChangeListener> advertiserIdentifierChangeListener = new ArrayList<>();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AndroidPlatform(Context context) {
        this.context = context;
        SensorbergApplication.getComponent().inject(this);

        permissionChecker = new PermissionChecker(context);

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothLowEnergySupported = true;
        } else {
            bluetoothLowEnergySupported = false;
            bluetoothAdapter = null;
        }

        postToServiceCounter = new PersistentIntegerCounter(settingsPreferences);
        pendingIntentStorage = new PendingIntentStorage(this);
    }


    private String getOrCreateInstallationIdentifier() {
        String value;

        String uuidString = settingsPreferences.getString(SENSORBERG_PREFERENCE_INSTALLATION_IDENTIFIER, null);
        if (uuidString != null) {
            value = uuidString;
        } else {
            value = uuidWithoutDashesString(UUID.randomUUID());
            persistInstallationIdentifier(value);
        }
        return value;
    }

    /**
     * Persists the installation identifier value to preferences.
     *
     * @param value - Value to save.
     */
    @SuppressLint("CommitPrefEdits")
    private void persistInstallationIdentifier(String value) {
        SharedPreferences.Editor editor = settingsPreferences.edit();
        editor.putString(SENSORBERG_PREFERENCE_INSTALLATION_IDENTIFIER, value);
        editor.commit();
    }

    /**
     * Persists the advertiser identifier value to preferences.
     *
     * @param value - Value to save.
     */
    @SuppressLint("CommitPrefEdits")
    private void persistAdvertiserIdentifier(String value) {
        SharedPreferences.Editor editor = settingsPreferences.edit();
        editor.putString(SENSORBERG_PREFERENCE_ADVERTISER_IDENTIFIER, value);
        editor.commit();
    }

    private static String getAppVersionString(Context context) {
        try {
            PackageInfo myInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return URLEncoder.encode(myInfo.versionName) + "/" + myInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return "<unknown>";
        } catch (NullPointerException e) {
            return "<unknown>";
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    private static String getAppLabel(Context application) {
        PackageManager pm = application.getPackageManager();
        ApplicationInfo ai = application.getApplicationInfo();
        return String.valueOf(pm.getApplicationLabel(ai));
    }

    @SuppressWarnings({"StringConcatenationInsideStringBufferAppend", "StringBufferReplaceableByString"})
    @Override
    public String getUserAgentString() {
        if (userAgentString == null){
            String packageName = context.getPackageName();
            String appLabel = URLEncoder.encode(getAppLabel(context));
            String appVersion = getAppVersionString(context);

            StringBuilder userAgent = new StringBuilder();
            userAgent.append(appLabel + "/" + packageName + "/" + appVersion);
            userAgent.append(" ");
            //noinspection deprecation old API compatability
            userAgent.append("(Android " + Build.VERSION.RELEASE + " "+  Build.CPU_ABI +")");
            userAgent.append(" ");
            userAgent.append("(" + Build.MANUFACTURER+ ":" + android.os.Build.MODEL + ":" + android.os.Build.PRODUCT + ")");
            userAgent.append(" ");
            userAgent.append("Sensorberg SDK " + BuildConfig.VERSION_NAME);
            userAgentString = userAgent.toString();
        }
        return userAgentString;
    }

    @Override
    public String getDeviceInstallationIdentifier() {
        if (deviceInstallationIdentifier == null) {
            deviceInstallationIdentifier = getOrCreateInstallationIdentifier();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                long timeBefore = System.currentTimeMillis();

                persistInstallationIdentifier(deviceInstallationIdentifier);
                for (DeviceInstallationIdentifierChangeListener listener : deviceInstallationIdentifierChangeListener) {
                    listener.deviceInstallationIdentifierChanged(deviceInstallationIdentifier);
                }

                Logger.log.verbose("Fetching installation ID took " + (System.currentTimeMillis() - timeBefore) + " millis");
            }
        }).start();

        return deviceInstallationIdentifier;
    }

    @Override
    public String getAdvertiserIdentifier() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                long timeBefore = System.currentTimeMillis();

                try {
                    AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    if (info == null || info.getId() == null){
                        Logger.log.logError("AdvertisingIdClient.getAdvertisingIdInfo returned null");
                        return;
                    }
                    if (info.isLimitAdTrackingEnabled()) {
                        return;
                    }

                    advertiserIdentifier = "google:" + info.getId();
                    persistAdvertiserIdentifier(advertiserIdentifier);

                    for (AdvertiserIdentifierChangeListener listener : advertiserIdentifierChangeListener) {
                        listener.advertiserIdentifierChanged((!info.isLimitAdTrackingEnabled()) ? advertiserIdentifier : "");
                    }

                } catch (IOException e) {
                    Logger.log.logError("Could not fetch the advertising identifier because of an IO Exception" , e);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Logger.log.logError("Play services are not available", e);
                } catch (GooglePlayServicesRepairableException e) {
                    Logger.log.logError("Play services are in need of repairs", e);
                } catch (Exception e){
                    Logger.log.logError("Could not fetch the advertising identifier because of an unknown error" , e);
                }
                Logger.log.verbose("Fetching the advertising identifier took " + (System.currentTimeMillis() - timeBefore) + " millis");
            }
        }).start();

        return advertiserIdentifier;
    }

    @Override
    public Transport getTransport() {
        if (asyncTransport == null) {
            asyncTransport = new OkHttpClientTransport(this, settings, OkVolley.newRequestQueue(context, shouldUseHttpCache), clock);
        }
        return asyncTransport;
    }

    @Override
    public boolean useSyncClient() {
        return false;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean isSyncEnabled() {
        if (permissionChecker.hasReadSyncSettingsPermissions()) {
            return ContentResolver.getMasterSyncAutomatically();
        } else {
            return true;
        }
    }

    @Override
    public boolean hasMinimumAndroidRequirements() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    @Override
    public void scheduleRepeating(int MSG_type, long value, TimeUnit timeUnit) {
        long millis = TimeUnit.MILLISECONDS.convert(value, timeUnit);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = getPendingIntent(MSG_type);
        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, clock.elapsedRealtime() + millis, millis, pendingIntent);
        repeatingPendingIntents.add(MSG_type);
    }

    private PendingIntent getPendingIntent(int MSG_type) {
        Intent intent = new Intent(context, GenericBroadcastReceiver.class);
        intent.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, MSG_type);
        intent.setAction("broadcast_repeating:///message_" + MSG_type);

        return PendingIntent.getBroadcast(context,
                -1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public void postToServiceDelayed(long delay, int type, Parcelable what, boolean surviveReboot) {
        int index = postToServiceCounter.next();
        postToServiceDelayed(delay, type, what, surviveReboot, index);
    }


    @Override
    public void postToServiceDelayed(long delayMillis, int type, Parcelable what, boolean surviveReboot, int index) {
        Bundle bundle = getScheduleBundle(index, type, what);
        scheduleIntent(index, delayMillis, bundle);

        if (surviveReboot){
            pendingIntentStorage.add(index, clock.now() + delayMillis, 0, bundle);
        }
    }

    private Bundle getScheduleBundle(int index, int type, Parcelable what) {
        Bundle bundle = new Bundle();
        bundle.putInt(SensorbergService.EXTRA_GENERIC_TYPE, type);
        bundle.putParcelable(SensorbergService.EXTRA_GENERIC_WHAT, what);
        bundle.putInt(SensorbergService.EXTRA_GENERIC_INDEX, index);
        return bundle;
    }

    @SuppressLint("NewApi")
    @Override
    public void scheduleIntent(long index, long delayInMillis, Bundle content) {
        PendingIntent pendingIntent = getPendingIntent(index, content);
        scheduleAlarm(delayInMillis, pendingIntent);
    }

    @Override
    public void postDeliverAtOrUpdate(Date deliverAt, BeaconEvent beaconEvent) {
        long delayInMillis = deliverAt.getTime() - clock.now();
        if (delayInMillis < 0){
            Logger.log.beaconResolveState(beaconEvent, "scheduled time is in the past, dropping event.");
            return;
        }
        int index  = postToServiceCounter.next();
        int hashcode = beaconEvent.hashCode();

        Bundle bundle = getScheduleBundle(index, SensorbergService.GENERIC_TYPE_BEACON_ACTION, beaconEvent);
        PendingIntent pendingIntent = getPendingIntent(hashcode, bundle, "DeliverAt");

        scheduleAlarm(delayInMillis, pendingIntent);
        pendingIntentStorage.add(index, deliverAt.getTime(), hashcode, bundle);
    }

    @SuppressLint("NewApi")
    private void scheduleAlarm(long delayInMillis, PendingIntent pendingIntent) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, clock.elapsedRealtime() + delayInMillis, settings.getMessageDelayWindowLength(), pendingIntent);
        } else {
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, clock.elapsedRealtime() + delayInMillis, pendingIntent);
        }
    }

    @Override
    public void clearAllPendingIntents() {
        pendingIntentStorage.clearAllPendingIntents();
    }

    @Override
    public void restorePendingIntents() {
        pendingIntentStorage.restorePendingIntents();
    }

    @Override
    public void removeStoredPendingIntent(int index) {
        pendingIntentStorage.removeStoredPendingIntent(index);
    }

    @Override
    public void addDeviceInstallationIdentifierChangeListener(DeviceInstallationIdentifierChangeListener listener) {
        this.deviceInstallationIdentifierChangeListener.add(listener);
    }

    @Override
    public void addAdvertiserIdentifierChangeListener(AdvertiserIdentifierChangeListener listener) {
        this.advertiserIdentifierChangeListener.add(listener);
    }

    @Override
    public boolean registerBroadcastReceiver() {
        if (!actionBroadcastReceiversRegistered) {
            List<BroadcastReceiver> broadcastReceiver = getBroadcastReceiver();
            if (broadcastReceiver.isEmpty()) {
                return false;
            }
            registerBroadcastReceiver(broadcastReceiver);
            actionBroadcastReceiversRegistered = true;
        }
        return true;
    }

    private PendingIntent getPendingIntent(long index, Bundle extras) {
        return getPendingIntent(index, extras, "");
    }

    private PendingIntent getPendingIntent(long index, Bundle extras, String prefix) {
        Intent intent = new Intent(context, genericBroadcastReceiverClass);
        if(extras != null){
            intent.putExtras(extras);
        }
        intent.setData(Uri.parse("sensorberg" + prefix + ":" + index));

        return PendingIntent.getBroadcast(context,
                -1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void unscheduleIntent(int index) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(getPendingIntent(index, null));
    }

    @Override
    public void cancelAllScheduledTimer() {
        for (Integer messageType: repeatingPendingIntents) {
            cancel(messageType);
        }
        repeatingPendingIntents.clear();
    }

    @Override
    public String getHostApplicationId() {
        return context.getPackageName();
    }

    @Override
    public void cancelServiceMessage(int index) {
        PendingIntent pendingIntent = getPendingIntent(index, new Bundle());
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    @Override
    public List<BroadcastReceiver> getBroadcastReceiver() {
        return ManifestParser.findBroadcastReceiver(context);
    }

    @Override
    public void registerBroadcastReceiver(List<BroadcastReceiver> broadcastReceiver) {
        for (BroadcastReceiver receiver : broadcastReceiver) {
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(ManifestParser.actionString));
        }
    }

    @Override
    public void cancel(int message){
        PendingIntent pendingIntent = getPendingIntent(message);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    /**
     * Returns a flag indicating whether Bluetooth is enabled.
     *
     * @return a flag indicating whether Bluetooth is enabled
     */
    @Override
    public boolean isBluetoothLowEnergyDeviceTurnedOn() {
        //noinspection SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement,SimplifiableIfStatement
        return bluetoothLowEnergySupported && (bluetoothAdapter.isEnabled());
    }

    /**
     * Returns a flag indicating whether Bluetooth is supported.
     *
     * @return a flag indicating whether Bluetooth is supported
     */
    @Override
    public boolean isBluetoothLowEnergySupported() {
        return bluetoothLowEnergySupported;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void startLeScan(BluetoothAdapter.LeScanCallback scanCallback) {
        if (bluetoothLowEnergySupported) {
            if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                //noinspection deprecation old API compatability
                bluetoothAdapter.startLeScan(getCrashCallBackWrapper());
                getCrashCallBackWrapper().setCallback(scanCallback);
                leScanRunning = true;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void stopLeScan() {
        if(bluetoothLowEnergySupported) {
            try {
                //noinspection deprecation old API compatability
                bluetoothAdapter.stopLeScan(getCrashCallBackWrapper());
            } catch (NullPointerException sentBySysteminternally) {
                Logger.log.logError("System bug throwing a NullPointerException internally.", sentBySysteminternally);
            } finally {
                leScanRunning = false;
                getCrashCallBackWrapper().setCallback(null);
            }
        }
    }

    @Override
    public boolean isLeScanRunning() {
        return leScanRunning;
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public boolean isBluetoothEnabled() {
        return bluetoothLowEnergySupported && bluetoothAdapter.isEnabled();
    }

    @Override
    public RunLoop getScannerRunLoop(RunLoop.MessageHandlerCallback callback) {
        return new AndroidHandler(callback);
    }

    @Override
    public RunLoop getResolverRunLoop(RunLoop.MessageHandlerCallback callback) {
        return new AndroidHandler(callback);
    }

    @Override
    public RunLoop getBeaconPublisherRunLoop(RunLoop.MessageHandlerCallback callback){
        return new AndroidHandler(callback);
    }

    private CrashCallBackWrapper getCrashCallBackWrapper() {
        if (crashCallBackWrapper == null) {
            if (bluetoothLowEnergySupported) {
                crashCallBackWrapper = new CrashCallBackWrapper(context);
            }
            else{
                crashCallBackWrapper = new CrashCallBackWrapper();
            }
        }
        return crashCallBackWrapper;
    }

}
