package com.sensorberg.sdk.internal;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.sensorberg.SensorbergApplication;
import com.sensorberg.android.okvolley.OkVolley;
import com.sensorberg.bluetooth.CrashCallBackWrapper;
import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.presenter.LocalBroadcastManager;
import com.sensorberg.sdk.presenter.ManifestParser;
import com.sensorberg.sdk.settings.Settings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import static com.sensorberg.utils.UUIDUtils.uuidWithoutDashesString;

public class AndroidPlatform implements Platform {

    private static final String SENSORBERG_PREFERENCE_INSTALLATION_IDENTIFIER = "com.sensorberg.preferences.installationUuidIdentifier";

    private static final String SENSORBERG_PREFERENCE_ADVERTISER_IDENTIFIER = "com.sensorberg.preferences.advertiserIdentifier";

    @Inject
    SharedPreferences settingsPreferences;

    @Inject
    Clock clock;

    @Inject
    PermissionChecker permissionChecker;

    @Inject
    ServiceScheduler mServiceScheduler;

    private final Context context;

    private CrashCallBackWrapper crashCallBackWrapper;

    private final BluetoothAdapter bluetoothAdapter;

    private final boolean bluetoothLowEnergySupported;

    private String userAgentString;

    private Transport asyncTransport;

    private String deviceInstallationIdentifier;

    private String advertiserIdentifier;

    private boolean leScanRunning = false;

    private Settings settings;

    private boolean shouldUseHttpCache = true;

    private static boolean actionBroadcastReceiversRegistered;

    private final ArrayList<DeviceInstallationIdentifierChangeListener> deviceInstallationIdentifierChangeListener = new ArrayList<>();

    private final ArrayList<AdvertiserIdentifierChangeListener> advertiserIdentifierChangeListener = new ArrayList<>();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AndroidPlatform(Context context) {
        this.context = context;
        SensorbergApplication.getComponent().inject(this);

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothLowEnergySupported = true;
        } else {
            bluetoothLowEnergySupported = false;
            bluetoothAdapter = null;
        }
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
        if (userAgentString == null) {
            String packageName = context.getPackageName();
            String appLabel = URLEncoder.encode(getAppLabel(context));
            String appVersion = getAppVersionString(context);

            StringBuilder userAgent = new StringBuilder();
            userAgent.append(appLabel + "/" + packageName + "/" + appVersion);
            userAgent.append(" ");
            //noinspection deprecation old API compatability
            userAgent.append("(Android " + Build.VERSION.RELEASE + " " + Build.CPU_ABI + ")");
            userAgent.append(" ");
            userAgent.append("(" + Build.MANUFACTURER + ":" + android.os.Build.MODEL + ":" + android.os.Build.PRODUCT + ")");
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
                    if (info == null || info.getId() == null) {
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
                    Logger.log.logError("Could not fetch the advertising identifier because of an IO Exception", e);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Logger.log.logError("Play services are not available", e);
                } catch (GooglePlayServicesRepairableException e) {
                    Logger.log.logError("Play services are in need of repairs", e);
                } catch (Exception e) {
                    Logger.log.logError("Could not fetch the advertising identifier because of an unknown error", e);
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

    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;
        mServiceScheduler.setSettings(settings);
    }

    @Override
    public String getHostApplicationId() {
        return context.getPackageName();
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
        if (bluetoothLowEnergySupported) {
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
    public RunLoop getBeaconPublisherRunLoop(RunLoop.MessageHandlerCallback callback) {
        return new AndroidHandler(callback);
    }

    private CrashCallBackWrapper getCrashCallBackWrapper() {
        if (crashCallBackWrapper == null) {
            if (bluetoothLowEnergySupported) {
                crashCallBackWrapper = new CrashCallBackWrapper(context);
            } else {
                crashCallBackWrapper = new CrashCallBackWrapper();
            }
        }
        return crashCallBackWrapper;
    }

}
