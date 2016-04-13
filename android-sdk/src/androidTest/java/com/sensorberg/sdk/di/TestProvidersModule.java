package com.sensorberg.sdk.di;

import com.sensorberg.di.ProvidersModule;
import com.sensorberg.sdk.testUtils.TestFileManager;

import android.app.Application;
import android.content.Context;

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
    @Singleton
    public TestFileManager provideTestFileManager(Context context) {
        return new TestFileManager(context);
    }

}
