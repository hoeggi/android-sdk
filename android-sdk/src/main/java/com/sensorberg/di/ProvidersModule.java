package com.sensorberg.di;

import com.sensorberg.bluetooth.CrashCallBackWrapper;
import com.sensorberg.sdk.internal.AndroidClock;
import com.sensorberg.sdk.internal.AndroidFileManager;
import com.sensorberg.sdk.internal.AndroidHandlerManager;
import com.sensorberg.sdk.internal.AndroidServiceScheduler;
import com.sensorberg.sdk.internal.PermissionChecker;
import com.sensorberg.sdk.internal.PersistentIntegerCounter;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.internal.interfaces.HandlerManager;
import com.sensorberg.sdk.internal.interfaces.ServiceScheduler;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

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
    public ServiceScheduler provideIntentScheduler(Context context, AlarmManager alarmManager, @Named("realClock") Clock clock, PersistentIntegerCounter persistentIntegerCounter) {
        return new AndroidServiceScheduler(context, alarmManager, clock, persistentIntegerCounter);
    }

    @Provides
    @Singleton
    public CrashCallBackWrapper provideCrashCallBackWrapper(Context context) {
        return new CrashCallBackWrapper(context);
    }

    @Provides
    @Singleton
    public HandlerManager provideHandlerManager() {
        return new AndroidHandlerManager();
    }
}
