package com.sensorberg.sdk.di;

import com.sensorberg.di.ProvidersModule;
import com.sensorberg.sdk.internal.PersistentIntegerCounter;
import com.sensorberg.sdk.internal.interfaces.Clock;
import com.sensorberg.sdk.testUtils.NoClock;
import com.sensorberg.sdk.testUtils.TestFileManager;
import com.sensorberg.sdk.testUtils.TestServiceScheduler;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TestProvidersModule extends ProvidersModule {

    public TestProvidersModule(Application app) {
        super(app);
    }

//    @Provides
//    @Singleton
//    public Clock provideClock() {
    //TODO use this in tests, extract from TestPlatform
//        return new CustomClock();
//    }

    @Provides
    @Named("noClock")
    @Singleton
    public Clock provideNoClock() {
        return NoClock.CLOCK;
    }

    @Provides
    @Singleton
    public TestFileManager provideTestFileManager(Context context) {
        return new TestFileManager(context);
    }

    @Provides
    @Singleton
    public TestServiceScheduler provideTestServiceScheduler(Context context, AlarmManager alarmManager, @Named("realClock") Clock clock,
            PersistentIntegerCounter persistentIntegerCounter) {
        return new TestServiceScheduler(context, alarmManager, clock, persistentIntegerCounter);
    }

}
