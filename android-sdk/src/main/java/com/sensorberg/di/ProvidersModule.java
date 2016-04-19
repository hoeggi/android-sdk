package com.sensorberg.di;

import com.sensorberg.android.okvolley.OkVolley;
import com.sensorberg.bluetooth.CrashCallBackWrapper;
import com.sensorberg.sdk.internal.AndroidBluetoothPlatform;
import com.sensorberg.sdk.internal.AndroidClock;
import com.sensorberg.sdk.internal.AndroidFileManager;
import com.sensorberg.sdk.internal.AndroidHandlerManager;
import com.sensorberg.sdk.internal.AndroidPlatformIdentifier;
import com.sensorberg.sdk.internal.AndroidServiceScheduler;
import com.sensorberg.sdk.internal.OkHttpClientTransport;
import com.sensorberg.sdk.internal.PermissionChecker;
import com.sensorberg.sdk.internal.PersistentIntegerCounter;
import com.sensorberg.sdk.internal.interfaces.BluetoothPlatform;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;
import com.sensorberg.sdk.internal.interfaces.Transport;
import com.sensorberg.sdk.settings.DefaultSettings;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ProvidersModule {

    private static final String SENSORBERG_PREFERENCE_IDENTIFIER = "com.sensorberg.preferences";

    private final Application application;

    public ProvidersModule(Application app) {
        application = app;
    }

    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    public SharedPreferences provideSettingsSharedPrefs(Context context) {
        return context.getSharedPreferences(SENSORBERG_PREFERENCE_IDENTIFIER, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    public NotificationManager provideNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @Named("realClock")
    @Singleton
    public Clock provideRealClock() {
        return new AndroidClock();
    }

    @Provides
    @Singleton
    public FileManager provideFileManager(Context context) {
        return new AndroidFileManager(context);
    }

    @Provides
    @Singleton
    public PermissionChecker providePermissionChecker(Context context) {
        return new PermissionChecker(context);
    }

    @Provides
    @Singleton
    public PersistentIntegerCounter providePersistentIntegerCounter(SharedPreferences sharedPreferences) {
        return new PersistentIntegerCounter(sharedPreferences);
    }

    @Provides
    @Singleton
    public AlarmManager provideAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Provides
    @Singleton
    public ServiceScheduler provideIntentScheduler(Context context, AlarmManager alarmManager, @Named("realClock") Clock clock,
            PersistentIntegerCounter persistentIntegerCounter) {
        return new AndroidServiceScheduler(context, alarmManager, clock, persistentIntegerCounter,
                DefaultSettings.DEFAULT_MESSAGE_DELAY_WINDOW_LENGTH);
    }

    @Provides
    @Singleton
    public CrashCallBackWrapper provideCrashCallBackWrapper(Context context) {
        return new CrashCallBackWrapper(context);
    }

    @Provides
    @Named("realHandlerManager")
    @Singleton
    public HandlerManager provideAndroidHandlerManager() {
        return new AndroidHandlerManager();
    }

    @Provides
    @Named("androidPlatformIdentifier")
    @Singleton
    public PlatformIdentifier provideAndroidPlatformIdentifier(Context ctx, SharedPreferences settingsSharedPrefs) {
        return new AndroidPlatformIdentifier(ctx, settingsSharedPrefs);
    }

    @Provides
    @Named("androidBluetoothPlatform")
    @Singleton
    public BluetoothPlatform provideAndroidBluetoothPlatform(BluetoothAdapter adapter, CrashCallBackWrapper wrapper) {
        return new AndroidBluetoothPlatform(adapter, wrapper);
    }

    @Provides
    @Singleton
    public BluetoothAdapter provideBluetoothAdapter(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            return bluetoothManager.getAdapter();
        } else {
            return null;
        }
    }

    @Provides
    @Named("realTransport")
    @Singleton
    public Transport provideRealTransport(Context context, @Named("realClock") Clock clock,
            @Named("androidPlatformIdentifier") PlatformIdentifier platformIdentifier) {
        return new OkHttpClientTransport(OkVolley.newRequestQueue(context, true), clock, platformIdentifier, false);
    }
}
